package com.bytedance.scene;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.bytedance.scene.navigation.NavigationScene;

/**
 * Created by JiangQi on 11/6/18.
 */
public class SceneLifecycleManager {
    private NavigationScene mNavigationScene;

    public void onActivityCreated(@NonNull Activity activity,
                                  @NonNull ViewGroup viewGroup,
                                  @NonNull NavigationScene navigationScene,
                                  @NonNull NavigationScene.NavigationSceneHost navigationSceneHost,
                                  @NonNull Scope.RootScopeFactory rootScopeFactory,
                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                  @Nullable Bundle savedInstanceState) {
        if (navigationScene.getState() != State.NONE) {
            throw new IllegalStateException("NavigationScene state must be NONE");
        }
        if (activity == null) {
            throw new NullPointerException("activity can't be null");
        }
        if (navigationScene == null) {
            throw new NullPointerException("viewGroup can't be null");
        }
        if (navigationScene == null) {
            throw new NullPointerException("navigationScene can't be null");
        }
        if (navigationScene == null) {
            throw new NullPointerException("navigationSceneHost can't be null");
        }
        if (navigationScene == null) {
            throw new NullPointerException("rootScopeFactory can't be null");
        }
        this.mNavigationScene = navigationScene;
        this.mNavigationScene.setRootScopeFactory(rootScopeFactory);
        this.mNavigationScene.setNavigationSceneHost(navigationSceneHost);
        this.mNavigationScene.setRootSceneComponentFactory(rootSceneComponentFactory);
        this.mNavigationScene.dispatchAttachActivity(activity);
        this.mNavigationScene.dispatchAttachScene(null);
        this.mNavigationScene.dispatchCreate(savedInstanceState);
        this.mNavigationScene.dispatchCreateView(savedInstanceState, viewGroup);
        viewGroup.addView(this.mNavigationScene.getView());
        this.mNavigationScene.dispatchActivityCreated(savedInstanceState);
    }

    public void onStart() {
        if (this.mNavigationScene.getState() != State.STOPPED) {
            throw new IllegalStateException("NavigationScene state must be STOPPED");
        }
        this.mNavigationScene.dispatchStart();
    }

    public void onResume() {
        if (this.mNavigationScene.getState() != State.STARTED) {
            throw new IllegalStateException("NavigationScene state must be STARTED");
        }
        this.mNavigationScene.dispatchResume();
    }

    public void onPause() {
        if (this.mNavigationScene.getState() != State.RESUMED) {
            throw new IllegalStateException("NavigationScene state must be RESUMED");
        }
        this.mNavigationScene.dispatchPause();
    }

    public void onStop() {
        if (this.mNavigationScene.getState() != State.STARTED) {
            throw new IllegalStateException("NavigationScene state must be STARTED");
        }
        this.mNavigationScene.dispatchStop();
    }

    public void onDestroyView() {
        if (this.mNavigationScene.getState() != State.STOPPED) {
            throw new IllegalStateException("NavigationScene state must be STOPPED");
        }
        this.mNavigationScene.dispatchDestroyView();
        this.mNavigationScene.dispatchDestroy();
        this.mNavigationScene.dispatchDetachScene();
        this.mNavigationScene.dispatchDetachActivity();
        this.mNavigationScene.setRootSceneComponentFactory(null);
        this.mNavigationScene.setNavigationSceneHost(null);
        this.mNavigationScene.setRootScopeFactory(null);
        this.mNavigationScene = null;
    }

    public void onSaveInstanceState(Bundle outState) {
        this.mNavigationScene.dispatchSaveInstanceState(outState);
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        this.mNavigationScene.dispatchViewStateRestored(savedInstanceState);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.mNavigationScene != null) {
            this.mNavigationScene.onConfigurationChanged(newConfig);
        }
    }
}
