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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.Log;
import android.view.ViewGroup;

import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.Utility;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 11/6/18.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class SceneLifecycleManager<T extends NavigationScene & SceneParent> {
    private static final boolean DEBUG = false;
    private static final String TAG = "SceneLifecycleManager";

    private T mScene;
    @NonNull
    private SceneLifecycleManagerState mState = SceneLifecycleManagerState.NONE;
    private boolean mSupportRestore = false;

    private enum SceneLifecycleManagerState {
        NONE, ACTIVITY_CREATED, START, RESUME, PAUSE, STOP
    }

    public void onActivityCreated(@NonNull Activity activity,
                                  @NonNull ViewGroup viewGroup,
                                  @NonNull T scene,
                                  @NonNull Scope.RootScopeFactory rootScopeFactory,
                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                  boolean supportRestore,
                                  @Nullable Bundle savedInstanceState) {
        if (this.mState != SceneLifecycleManagerState.NONE) {
            throw new IllegalStateException("invoke onDestroyView() first, current state " + this.mState.toString());
        }

        Utility.requireNonNull(activity, "activity can't be null");
        Utility.requireNonNull(viewGroup, "viewGroup can't be null");
        Utility.requireNonNull(scene, "scene can't be null");
        Utility.requireNonNull(rootScopeFactory, "rootScopeFactory can't be null");

        if (scene.getState() != State.NONE) {
            throw new IllegalStateException("Scene state must be " + State.NONE.name);
        }

        this.mSupportRestore = supportRestore;
        if (!this.mSupportRestore && savedInstanceState != null) {
            throw new IllegalArgumentException("savedInstanceState should be null when not support restore");
        }

        this.mState = SceneLifecycleManagerState.ACTIVITY_CREATED;
        log("onActivityCreated");

        this.mScene = scene;
        if (!this.mSupportRestore) {
            this.mScene.disableSupportRestore();
        }
        this.mScene.setRootScopeFactory(rootScopeFactory);
        this.mScene.setRootSceneComponentFactory(rootSceneComponentFactory);
        this.mScene.dispatchAttachActivity(activity);
        this.mScene.dispatchAttachScene(null);
        this.mScene.dispatchCreate(savedInstanceState);
        this.mScene.dispatchCreateView(savedInstanceState, viewGroup);
        viewGroup.addView(this.mScene.requireView(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.mScene.dispatchActivityCreated(savedInstanceState);
    }

    public void onStart() {
        if (this.mState != SceneLifecycleManagerState.ACTIVITY_CREATED && this.mState != SceneLifecycleManagerState.STOP) {
            throw new IllegalStateException("invoke onActivityCreated() or onStop() first, current state " + this.mState.toString());
        }
        this.mState = SceneLifecycleManagerState.START;
        log("onStart");
        this.mScene.dispatchStart();
    }

    public void onResume() {
        if (this.mState != SceneLifecycleManagerState.START && this.mState != SceneLifecycleManagerState.PAUSE) {
            throw new IllegalStateException("invoke onStart() or onPause() first, current state " + this.mState.toString());
        }
        this.mState = SceneLifecycleManagerState.RESUME;
        log("onResume");
        this.mScene.dispatchResume();
    }

    public void onPause() {
        if (this.mState != SceneLifecycleManagerState.RESUME) {
            throw new IllegalStateException("invoke onResume() first, current state " + this.mState.toString());
        }
        this.mState = SceneLifecycleManagerState.PAUSE;
        log("onPause");
        this.mScene.dispatchPause();
    }

    public void onStop() {
        if (this.mState != SceneLifecycleManagerState.PAUSE && this.mState != SceneLifecycleManagerState.START) {
            throw new IllegalStateException("invoke onPause() or onStart() first, current state " + this.mState.toString());
        }
        this.mState = SceneLifecycleManagerState.STOP;
        log("onStop");
        this.mScene.dispatchStop();
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
        this.mScene.dispatchDestroyView();
        this.mScene.dispatchDestroy();
        this.mScene.dispatchDetachScene();
        this.mScene.dispatchDetachActivity();
        this.mScene.setRootSceneComponentFactory(null);
        this.mScene.setRootScopeFactory(null);
        this.mScene = null;
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
        this.mScene.dispatchSaveInstanceState(outState);
    }

    private void log(@NonNull String log) {
        if (DEBUG) {
            Log.d(TAG + "#" + hashCode(), log);
        }
    }
}
