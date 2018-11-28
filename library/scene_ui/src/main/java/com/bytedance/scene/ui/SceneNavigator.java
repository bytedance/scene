package com.bytedance.scene.ui;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.interfaces.PushResultCallback;
import com.bytedance.scene.utlity.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 9/3/18.
 */
public class SceneNavigator {
    public static interface NormalHook {
        Intent getIntent(Context context, int themeId, Class<? extends Scene> clazz, Bundle bundle);
    }

    public static interface ResultHook {
        Intent getIntent(Context context, int themeId, Class<? extends Scene> clazz, Bundle bundle,
                         final PushResultCallback pushResultCallback);
    }

    private Context mContext;
    private Activity mHostActivity;
    @StyleRes
    private int mThemeResId = -1;
    private List<SceneNavigationContainer> mContainerList;

    public SceneNavigator(@NonNull Context context, @StyleRes int themeResId) {
        this(context, themeResId, new ArrayList<SceneNavigationContainer>());
    }

    public SceneNavigator(@NonNull Context context, @StyleRes int themeResId, @Nullable SceneNavigationContainer container) {
        this(context, themeResId, toList(container));
    }

    private static List<SceneNavigationContainer> toList(SceneNavigationContainer container) {
        List<SceneNavigationContainer> list = new ArrayList<>();
        if (container != null) {
            list.add(container);
        }
        return list;
    }

    public SceneNavigator(@NonNull Context context, @StyleRes int themeResId, @Nullable List<SceneNavigationContainer> containerList) {
        mContext = context;
        mThemeResId = themeResId;
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                mHostActivity = (Activity) context;
                break;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        this.mContainerList = containerList;
    }

    public void startScene(@NonNull Class<? extends Scene> clazz, @Nullable Bundle bundle) {
        startScene(clazz, bundle, null);
    }

    public void startScene(@NonNull Class<? extends Scene> clazz, @Nullable Bundle bundle, @Nullable NormalHook normalHook) {
        //当前Activity销毁或者正在销毁直接返回
        if (mHostActivity != null) {
            if (!Utility.isActivityStatusValid(mHostActivity)) {
                return;
            }
        }

        SceneNavigationContainer container = getVisibleSceneContainerActivity();
        if (container != null) {
            container.getNavigationScene().push(clazz, bundle);
            return;
        }
        Intent intent = normalHook != null ? normalHook.getIntent(mContext, mThemeResId, clazz, bundle) : null;
        if (intent == null) {
            intent = SceneContainerActivity.newIntent(mContext, mThemeResId, clazz, bundle);
        }
        checkAndStart(intent);
    }

    public void startSceneForResult(@NonNull Class<? extends Scene> clazz, @Nullable Bundle bundle, @NonNull PushResultCallback pushResultCallback) {
        startSceneForResult(clazz, bundle, pushResultCallback, null);
    }

    public void startSceneForResult(@NonNull Class<? extends Scene> clazz, @Nullable Bundle bundle, @NonNull PushResultCallback pushResultCallback,
                                    @Nullable ResultHook resultHook) {
        //当前Activity销毁或者正在销毁直接返回
        if (mHostActivity != null) {
            if (!Utility.isActivityStatusValid(mHostActivity)) {
                pushResultCallback.onResult(null);
                return;
            }
        }

        SceneNavigationContainer container = getVisibleSceneContainerActivity();
        if (container != null) {
            container.getNavigationScene().push(clazz, bundle,
                    new PushOptions.Builder().setPushResultCallback(pushResultCallback).build());
            return;
        }

        Intent intent = resultHook != null ? resultHook.getIntent(mContext, mThemeResId, clazz, bundle, pushResultCallback) : null;
        if (intent == null) {
            intent = SceneContainerActivity.newIntentForResult(mContext, mThemeResId, clazz, bundle, pushResultCallback);
        }
        checkAndStart(intent);
    }

    private SceneNavigationContainer getVisibleSceneContainerActivity() {
        SceneNavigationContainer target = null;
        if (mHostActivity instanceof SceneNavigationContainer) {
            target = ((SceneNavigationContainer) mHostActivity);
            if (!isSceneContainerValidToUse(target)) {
                target = null;
            }
        }

        if (target == null && mContainerList != null) {
            for (SceneNavigationContainer container : mContainerList) {
                if (isSceneContainerValidToUse(container)) {
                    target = container;
                    break;
                }
            }
        }

        if (target == null) {
            List<SceneContainerActivity> activityList = new ArrayList<>(SceneContainerActivity.sInstance);
            SceneContainerActivity currentTopContainerActivity = activityList.size() > 0 ? activityList.get(activityList.size() - 1) : null;
            if (currentTopContainerActivity != null && isSceneContainerValidToUse(currentTopContainerActivity) && Utility.isActivityStatusValid(currentTopContainerActivity)) {
                target = currentTopContainerActivity;
            }
        }

        return target;
    }

    protected boolean isSceneContainerValidToUse(SceneNavigationContainer container) {
        return container.isVisible() && container.getThemeId() == mThemeResId && container.getNavigationScene() != null;
    }

    private void checkAndStart(Intent intent) {
        if (mHostActivity != null) {
            mHostActivity.startActivity(intent);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }
}
