package com.bytedance.scene.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.SceneDelegate;
import com.bytedance.scene.NavigationSceneUtility;
import com.bytedance.scene.Scene;
import com.bytedance.scene.SingeProcessMessengerHandler;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.interfaces.PushResultCallback;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scene.utlity.AnimatorUtility;
import com.bytedance.scene.utlity.CancellationSignal;
import com.bytedance.scene.utlity.NonNullPair;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by JiangQi on 9/3/18.
 *
 * Can not use singleTop here, because we need to determine whether the theme is the same,
 * and sInstance can achieve the same effect of the singleTop mode as well.
 *
 * Todo: Handle multiple startup
 */
public class SceneContainerActivity extends AppCompatActivity implements SceneNavigationContainer {
    private static final String EXTRA_CLASS_NAME = "class_name";
    private static final String EXTRA_THEME = "theme";
    private static final String EXTRA_ARGUMENTS = "arguments";

    public static Intent newIntent(Context context, int themeId, Class<? extends Scene> clazz, Bundle bundle) {
        Intent intent = new Intent(context, SceneContainerActivity.class);
        intent.putExtra(EXTRA_CLASS_NAME, clazz.getName());
        intent.putExtra(EXTRA_THEME, themeId);
        intent.putExtra(EXTRA_ARGUMENTS, bundle);
        return intent;
    }

    private static final Set<PushResultCallback> PUSH_RESULT_CALLBACK_SET = new HashSet<>();

    public static Intent newIntentForResult(Context context, int themeId, Class<? extends Scene> clazz, Bundle bundle,
                                            final PushResultCallback pushResultCallback) {
        Intent intent = new Intent(context, SceneContainerActivity.class);
        intent.putExtra(EXTRA_CLASS_NAME, clazz.getName());
        intent.putExtra(EXTRA_THEME, themeId);
        intent.putExtra(EXTRA_ARGUMENTS, bundle);
        PUSH_RESULT_CALLBACK_SET.add(pushResultCallback);
        final WeakReference<PushResultCallback> reference = new WeakReference<>(pushResultCallback);
        SingeProcessMessengerHandler.put(intent, new SingeProcessMessengerHandler.Callback() {
            @Override
            public void onResult(Object result) {
                PushResultCallback callback = reference.get();
                if (callback != null) {
                    callback.onResult(result);
                    PUSH_RESULT_CALLBACK_SET.remove(callback);
                }
            }
        });
        return intent;
    }

    public static final List<SceneContainerActivity> sInstance = new ArrayList<>();
    private SceneDelegate mDelegate;
    private boolean mIsVisible = false;
    @StyleRes
    public int mThemeResId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeId = getIntent().getIntExtra(EXTRA_THEME, -1);
        if (themeId != -1) {
            setTheme(themeId);
        }
        this.mThemeResId = themeId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        sInstance.add(this);
        SingeProcessMessengerHandler singeProcessMessengerHandler = SingeProcessMessengerHandler.from(getIntent());
        if (singeProcessMessengerHandler != null) {
            this.mDelegate = NavigationSceneUtility.setupWithActivity(this, savedInstanceState,
                    new NavigationSceneOptions(DelegateScene.class, null), false);
        } else {
            NonNullPair<? extends Class<? extends Scene>, Bundle> pair = getSceneDataFromIntent(getIntent());
            this.mDelegate = NavigationSceneUtility.setupWithActivity(this, savedInstanceState,
                    new NavigationSceneOptions(pair.first, pair.second), false);
        }
    }

    public static class DelegateScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireActivity());
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Intent intent = requireActivity().getIntent();
            NonNullPair<? extends Class<? extends Scene>, Bundle> pair = getSceneDataFromIntent(intent);
            getNavigationScene().push(pair.first,
                    pair.second, new PushOptions.Builder().setAnimation(new KeepAnimationExecutor())
                            .setPushResultCallback(new PushResultCallback() {
                                @Override
                                public void onResult(@Nullable Object result) {
                                    SingeProcessMessengerHandler singeProcessMessengerHandler = SingeProcessMessengerHandler.from(requireActivity().getIntent());
                                    singeProcessMessengerHandler.sendResult(result);
                                    requireActivity().finish();
                                }
                            }).build());
        }
    }

    private static class KeepAnimationExecutor extends NavigationAnimationExecutor {
        @Override
        public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
            return true;
        }

        @Override
        public void executePushChangeCancelable(@NonNull AnimationInfo fromInfo, @NonNull AnimationInfo toInfo, @NonNull Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
            endAction.run();
        }

        @Override
        public void executePopChangeCancelable(@NonNull AnimationInfo fromInfo, @NonNull AnimationInfo toInfo, @NonNull Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
            final View fromView = fromInfo.mSceneView;
            final View toView = toInfo.mSceneView;

            AnimatorUtility.resetViewStatus(fromView);
            AnimatorUtility.resetViewStatus(toView);

            fromView.setVisibility(View.VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mAnimationViewGroup.getOverlay().add(fromView);
            } else {
                mAnimationViewGroup.addView(fromView);
            }
            endAction.run();
        }
    }

    @NonNull
    private static NonNullPair<? extends Class<? extends Scene>, Bundle> getSceneDataFromIntent(Intent intent) {
        Class<? extends Scene> clazz = null;
        try {
            clazz = (Class<? extends Scene>) Class.forName(intent.getStringExtra(EXTRA_CLASS_NAME));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        Bundle bundle = intent.getBundleExtra(EXTRA_ARGUMENTS);
        return NonNullPair.create(clazz, bundle);
    }

    @Override
    public void onBackPressed() {
        if (!this.mDelegate.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public NavigationScene getNavigationScene() {
        return this.mDelegate.getNavigationScene();
    }

    @Override
    public int getThemeId() {
        return this.mThemeResId;
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.mIsVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsVisible = false;
    }

    @Override
    public boolean isVisible() {
        return this.mIsVisible;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sInstance.remove(this);
    }
}
