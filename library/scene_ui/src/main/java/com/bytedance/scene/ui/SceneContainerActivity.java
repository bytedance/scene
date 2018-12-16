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
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.interfaces.PushResultCallback;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scene.utlity.NonNullPair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 9/3/18.
 * <p>
 * todo Activity怎么从Scene拿结果
 * <p>
 * todo 同时启动多个怎么处理
 * 不能用singleTop因为还需要判断主题是否一致，而且sInstance可以做到singleTop的效果
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

    public static Intent newIntentForResult(Context context, int themeId, Class<? extends Scene> clazz, Bundle bundle,
                                            final PushResultCallback pushResultCallback) {
        Intent intent = new Intent(context, SceneContainerActivity.class);
        intent.putExtra(EXTRA_CLASS_NAME, clazz.getName());
        intent.putExtra(EXTRA_THEME, themeId);
        intent.putExtra(EXTRA_ARGUMENTS, bundle);
        SingeProcessMessengerHandler.put(intent, new SingeProcessMessengerHandler.Callback() {
            @Override
            public void onResult(Object result) {
                pushResultCallback.onResult(result);
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
                    new NavigationSceneOptions().setRootScene(DelegateScene.class, null), false);
        } else {
            NonNullPair<? extends Class<? extends Scene>, Bundle> pair = getSceneDataFromIntent(getIntent());
            this.mDelegate = NavigationSceneUtility.setupWithActivity(this, savedInstanceState,
                    new NavigationSceneOptions().setRootScene(pair.first, pair.second), false);
        }
    }

    public static class DelegateScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireActivity());
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Intent intent = requireActivity().getIntent();
            NonNullPair<? extends Class<? extends Scene>, Bundle> pair = getSceneDataFromIntent(intent);
            getNavigationScene().push(pair.first,
                    pair.second, new PushOptions.Builder().setAnimation(new NoAnimationExecutor())
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
