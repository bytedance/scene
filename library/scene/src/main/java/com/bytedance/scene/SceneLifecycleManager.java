/*
 * Copyright (C) 2019 ByteDance Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bytedance.scene;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;

import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.Utility;

/**
 * Created by JiangQi on 11/6/18.
 */
public class SceneLifecycleManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "SceneLifecycleManager";

    private NavigationScene mNavigationScene;
    @NonNull
    private SceneLifecycleManagerState mState = SceneLifecycleManagerState.NONE;
    private boolean mSupportRestore = false;

    private enum SceneLifecycleManagerState {
        NONE, ACTIVITY_CREATED, START, RESUME, PAUSE, STOP
    }

    public void onActivityCreated(@NonNull Activity activity,
                                  @NonNull ViewGroup viewGroup,
                                  @NonNull NavigationScene navigationScene,
                                  @NonNull NavigationScene.NavigationSceneHost navigationSceneHost,
                                  @NonNull Scope.RootScopeFactory rootScopeFactory,
                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                  @Nullable Bundle savedInstanceState) {
        if (this.mState != SceneLifecycleManagerState.NONE) {
            throw new IllegalStateException("invoke onDestroyView() first, current state " + this.mState.toString());
        }

        Utility.requireNonNull(activity, "activity can't be null");
        Utility.requireNonNull(viewGroup, "viewGroup can't be null");
        Utility.requireNonNull(navigationScene, "navigationScene can't be null");
        Utility.requireNonNull(navigationSceneHost, "navigationSceneHost can't be null");
        Utility.requireNonNull(rootScopeFactory, "rootScopeFactory can't be null");

        if (navigationScene.getState() != State.NONE) {
            throw new IllegalStateException("NavigationScene state must be " + State.NONE.name);
        }

        this.mSupportRestore = navigationSceneHost.isSupportRestore();
        if (!this.mSupportRestore && savedInstanceState != null) {
            throw new IllegalArgumentException("savedInstanceState should be null when not support restore");
        }

        this.mState = SceneLifecycleManagerState.ACTIVITY_CREATED;
        log("onActivityCreated");

        this.mNavigationScene = navigationScene;
        this.mNavigationScene.setRootScopeFactory(rootScopeFactory);
        this.mNavigationScene.setNavigationSceneHost(navigationSceneHost);
        this.mNavigationScene.setRootSceneComponentFactory(rootSceneComponentFactory);
        this.mNavigationScene.dispatchAttachActivity(activity);
        this.mNavigationScene.dispatchAttachScene(null);
        this.mNavigationScene.dispatchCreate(savedInstanceState);
        this.mNavigationScene.dispatchCreateView(savedInstanceState, viewGroup);
        viewGroup.addView(this.mNavigationScene.getView(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.mNavigationScene.dispatchActivityCreated(savedInstanceState);
    }

    public void onStart() {
        if (this.mState != SceneLifecycleManagerState.ACTIVITY_CREATED && this.mState != SceneLifecycleManagerState.STOP) {
            throw new IllegalStateException("invoke onActivityCreated() or onStop() first, current state " + this.mState.toString());
        }
        this.mState = SceneLifecycleManagerState.START;
        log("onStart");
        this.mNavigationScene.dispatchStart();
    }

    public void onResume() {
        if (this.mState != SceneLifecycleManagerState.START && this.mState != SceneLifecycleManagerState.PAUSE) {
            throw new IllegalStateException("invoke onStart() or onPause() first, current state " + this.mState.toString());
        }
        this.mState = SceneLifecycleManagerState.RESUME;
        log("onResume");
        this.mNavigationScene.dispatchResume();
    }

    public void onPause() {
        if (this.mState != SceneLifecycleManagerState.RESUME) {
            throw new IllegalStateException("invoke onResume() first, current state " + this.mState.toString());
        }
        this.mState = SceneLifecycleManagerState.PAUSE;
        log("onPause");
        this.mNavigationScene.dispatchPause();
    }

    public void onStop() {
        if (this.mState != SceneLifecycleManagerState.PAUSE && this.mState != SceneLifecycleManagerState.START) {
            throw new IllegalStateException("invoke onPause() or onStart() first, current state " + this.mState.toString());
        }
        this.mState = SceneLifecycleManagerState.STOP;
        log("onStop");
        this.mNavigationScene.dispatchStop();
    }

    /**
     * extreme cases:
     * <p>
     * 1.If Scene's host is a Support Library Fragment, this host Fragment can be removed by commit() in Activity onCreate(), then Fragment will be destroyed in AppCompatActivity onStart(),
     * this Fragment's lifecycle is from onActivityCreated() to onDestroyView(), skip onStart()/onResume()/onPause()/onStop()
     * <p>
     * 2.After NavigationSceneUtility/NavigationSceneCompatUtility bind Scene in onCreate()/onActivityCreated(), invoke returned SceneDelegate's abandon() in onStart(),
     * LifCycleFragment/LifeCycleCompatFragment will skip onStart()/onResume()/onPause()/onStop() too
     */
    public void onDestroyView() {
        if (this.mState != SceneLifecycleManagerState.STOP && this.mState != SceneLifecycleManagerState.ACTIVITY_CREATED) {
            throw new IllegalStateException("invoke onStop() or onActivityCreated() first, current state " + this.mState.toString());
        }
        this.mState = SceneLifecycleManagerState.NONE;
        log("onDestroyView");
        this.mNavigationScene.dispatchDestroyView();
        this.mNavigationScene.dispatchDestroy();
        this.mNavigationScene.dispatchDetachScene();
        this.mNavigationScene.dispatchDetachActivity();
        this.mNavigationScene.setRootSceneComponentFactory(null);
        this.mNavigationScene.setNavigationSceneHost(null);
        this.mNavigationScene.setRootScopeFactory(null);
        this.mNavigationScene = null;
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        Utility.requireNonNull(outState, "outState can't be null");
        if (this.mState == SceneLifecycleManagerState.NONE) {
            throw new IllegalStateException("invoke onActivityCreated() first, current state " + this.mState.toString());
        }
        if (!this.mSupportRestore) {
            throw new IllegalArgumentException("cant invoke onSaveInstanceState when not support restore");
        }
        log("onSaveInstanceState");
        this.mNavigationScene.dispatchSaveInstanceState(outState);
    }

    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Utility.requireNonNull(newConfig, "newConfig can't be null");
        log("onConfigurationChanged");
        if (this.mNavigationScene != null) {
            this.mNavigationScene.onConfigurationChanged(newConfig);
        }
    }

    private void log(@NonNull String log) {
        if (DEBUG) {
            Log.d(TAG + "#" + hashCode(), log);
        }
    }
}
