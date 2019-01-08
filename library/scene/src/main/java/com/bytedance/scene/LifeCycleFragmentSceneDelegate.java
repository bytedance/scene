package com.bytedance.scene;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.bytedance.scene.navigation.NavigationScene;

/**
 * Created by JiangQi on 9/11/18.
 */
public class LifeCycleFragmentSceneDelegate implements SceneDelegate, NavigationSceneAvailableCallback {
    private Activity mActivity;
    private LifeCycleFragment mLifeCycleFragment;

    private NavigationScene mNavigationScene;
    private NavigationSceneAvailableCallback mNavigationSceneAvailableCallback;

    LifeCycleFragmentSceneDelegate(@NonNull Activity activity, @NonNull LifeCycleFragment lifeCycleFragment) {
        this.mActivity = activity;
        this.mLifeCycleFragment = lifeCycleFragment;
    }

    @Override
    public boolean onBackPressed() {
        NavigationScene navigationScene = mLifeCycleFragment.getNavigationScene();
        return navigationScene != null && navigationScene.onBackPressed();
    }

    @Override
    public NavigationScene getNavigationScene() {
        return mLifeCycleFragment.getNavigationScene();
    }

    @Override
    public final void onNavigationSceneAvailable(NavigationScene navigationScene) {
        this.mNavigationScene = navigationScene;
        if (this.mNavigationSceneAvailableCallback != null) {
            this.mNavigationSceneAvailableCallback.onNavigationSceneAvailable(navigationScene);
        }
    }

    @Override
    public final void setNavigationSceneAvailableCallback(NavigationSceneAvailableCallback callback) {
        this.mNavigationSceneAvailableCallback = callback;
        if (this.mNavigationScene != null) {
            this.mNavigationSceneAvailableCallback.onNavigationSceneAvailable(this.mNavigationScene);
        }
    }
}
