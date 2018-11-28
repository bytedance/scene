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
import android.widget.FrameLayout;

import com.bytedance.scene.navigation.NavigationScene;

/**
 * Created by JiangQi on 11/6/18.
 */
public abstract class LifeCycleFrameLayout extends FrameLayout implements NavigationScene.NavigationSceneHost {
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

    private NavigationScene mNavigationScene;
    private final SceneLifecycleManager mLifecycleManager = new SceneLifecycleManager();

    public void setNavigationScene(NavigationScene rootScene) {
        this.mNavigationScene = rootScene;
    }

    public NavigationScene getNavigationScene() {
        return mNavigationScene;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.mNavigationScene.onActivityResult(requestCode, resultCode, data);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        this.mNavigationScene.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Activity activity = null;
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                activity = (Activity) context;
                break;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }

        this.mLifecycleManager.onActivityCreated(activity,
                this,
                this.mNavigationScene,
                this,
                rootScopeFactory,
                null,
                savedInstanceState);
    }

    private final Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
        @Override
        public Scope getRootScope() {
            return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
        }
    };

    public void onStart() {
        this.mLifecycleManager.onStart();
    }

    public void onResume() {
        this.mLifecycleManager.onResume();
    }

    public void onPause() {
        this.mLifecycleManager.onPause();
    }

    public void onStop() {
        this.mLifecycleManager.onStop();
    }

    public void onDestroyView() {
        this.mLifecycleManager.onDestroyView();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mLifecycleManager.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean isSupportRestore() {
        return false;
    }
}
