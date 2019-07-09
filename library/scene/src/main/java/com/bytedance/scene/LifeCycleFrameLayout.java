package com.bytedance.scene;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.bytedance.scene.navigation.NavigationScene;

/**
 * Created by JiangQi on 11/6/18.
 */
public abstract class LifeCycleFrameLayout extends FrameLayout implements NavigationScene.NavigationSceneHost {
    private static final boolean DEBUG = false;
    private static final String TAG = "LifeCycleFrameLayout";

    public LifeCycleFrameLayout(@NonNull Context context) {
        super(context);
    }

    public LifeCycleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LifeCycleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LifeCycleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Nullable
    private NavigationScene mNavigationScene;
    @Nullable
    private SceneComponentFactory mRootSceneComponentFactory;
    @NonNull
    private Scope.RootScopeFactory mRootScopeFactory = new Scope.RootScopeFactory() {
        @Override
        public Scope getRootScope() {
            return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
        }
    };
    private final SceneLifecycleManager mLifecycleManager = new SceneLifecycleManager();

    /**
     * 为了解决手动管理生命周期和自动用Fragment管理生命周期时onViewStateRestored时机不同，Activity中先onStart再onRestoreInstanceState，
     * Fragment中先onViewStateRestored再onStart，Scene的流程中，统一都是先onViewStateRestored再onStart
     */
    private boolean mDelayOnStartInvokeAfterOnViewStateRestored = false;
    private boolean mInvokeOnStartMethod = false;

    public void setNavigationScene(@NonNull NavigationScene rootScene) {
        this.mNavigationScene = rootScene;
    }

    public void setRootSceneComponentFactory(@NonNull SceneComponentFactory rootSceneComponentFactory) {
        this.mRootSceneComponentFactory = rootSceneComponentFactory;
    }

    public void setRootScopeFactory(@NonNull Scope.RootScopeFactory rootScopeFactory) {
        this.mRootScopeFactory = rootScopeFactory;
    }

    @Nullable
    public NavigationScene getNavigationScene() {
        return mNavigationScene;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.mNavigationScene != null) {
            this.mNavigationScene.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (this.mNavigationScene != null) {
            this.mNavigationScene.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (this.mNavigationScene == null) {
            throw new NullPointerException("NavigationScene is null");
        }

        Activity activity = null;
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                activity = (Activity) context;
                break;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }

        if (activity == null) {
            throw new IllegalStateException("cant find Activity attached to this View");
        }

        this.mLifecycleManager.onActivityCreated(activity,
                this,
                this.mNavigationScene,
                this,
                this.mRootScopeFactory,
                this.mRootSceneComponentFactory,
                isSupportRestore() ? savedInstanceState : null);
        this.mDelayOnStartInvokeAfterOnViewStateRestored = isSupportRestore() && savedInstanceState != null;
        this.mInvokeOnStartMethod = false;
    }

    public void onViewStateRestored(@NonNull Bundle savedInstanceState) {
        if (!isSupportRestore()) {
            return;
        }
        this.mLifecycleManager.onViewStateRestored(savedInstanceState);
        if (this.mDelayOnStartInvokeAfterOnViewStateRestored && this.mInvokeOnStartMethod) {
            this.mLifecycleManager.onStart();
            this.mInvokeOnStartMethod = false;
        }
        this.mDelayOnStartInvokeAfterOnViewStateRestored = false;
    }

    public void onStart() {
        if (this.mDelayOnStartInvokeAfterOnViewStateRestored) {
            this.mInvokeOnStartMethod = true;
            return;
        }
        this.mLifecycleManager.onStart();
    }

    public void onResume() {
        if (this.mDelayOnStartInvokeAfterOnViewStateRestored) {
            throw new IllegalStateException("when app is restored, invoke onViewStateRestored before onResume");
        }
        this.mLifecycleManager.onResume();
    }

    public void onPause() {
        this.mLifecycleManager.onPause();
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (!isSupportRestore()) {
            return;
        }
        this.mLifecycleManager.onSaveInstanceState(outState);
    }

    public void onStop() {
        this.mLifecycleManager.onStop();
    }

    public void onDestroyView() {
        this.mLifecycleManager.onDestroyView();
    }

    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mLifecycleManager.onConfigurationChanged(newConfig);
    }

    private void log(String log) {
        if (DEBUG) {
            Log.d(TAG, log);
        }
    }
}
