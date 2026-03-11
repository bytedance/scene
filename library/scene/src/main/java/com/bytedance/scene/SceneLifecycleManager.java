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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.bytedance.scene.exceptions.OnSaveInstanceStateMethodMissingException;
import com.bytedance.scene.utlity.Utility;


/**
 * Created by JiangQi on 11/6/18.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class SceneLifecycleManager<T extends Scene & SceneParent> {
    private static final boolean DEBUG = false;
    private static final String TAG = "SceneLifecycleManager";
    private static final String SCENE_LIFECYCLE_MANAGER_ON_SAVE_INSTANCE_STATE_TAG = "SceneLifecycleManager_onSaveInstanceState_TAG";

    private static final String TRACE_ACTIVITY_CREATED_TAG = "SceneLifecycleDispatcher#OnActivityCreated";
    private static final String TRACE_START_TAG = "SceneLifecycleDispatcher#OnStart";
    private static final String TRACE_RESUME_TAG = "SceneLifecycleDispatcher#OnResume";
    private static final String TRACE_PAUSE_TAG = "SceneLifecycleDispatcher#OnPause";
    private static final String TRACE_STOP_TAG = "SceneLifecycleDispatcher#OnStop";
    private static final String TRACE_DESTROY_VIEW_TAG = "SceneLifecycleDispatcher#OnDestroyView";
    private static final String TRACE_SAVE_INSTANCE_STATE_TAG = "SceneLifecycleDispatcher#OnSaveInstance";

    private T mScene;
    @NonNull
    private SceneLifecycleManagerState mState = SceneLifecycleManagerState.NONE;
    private boolean mSupportRestore = false;
    private SceneStateSaveStrategy mSceneStateSaveStrategy = null;
    private boolean mStateSaved = false;

    private enum SceneLifecycleManagerState {
        NONE, ACTIVITY_CREATED, START, RESUME, PAUSE, STOP
    }

    public void onActivityCreated(@NonNull Activity activity,
                                  @NonNull ViewGroup viewGroup,
                                  @NonNull T scene,
                                  @NonNull Scope.RootScopeFactory rootScopeFactory,
                                  boolean supportRestore,
                                  @Nullable Bundle savedInstanceState) {
        this.onActivityCreated(activity, viewGroup, scene, rootScopeFactory, null, supportRestore, savedInstanceState);
    }

    public void onActivityCreated(@NonNull Activity activity,
                                  @NonNull ViewFinder viewFinder,
                                  @IdRes int sceneContainerViewId,
                                  @NonNull T scene,
                                  @NonNull Scope.RootScopeFactory rootScopeFactory,
                                  @Nullable SceneStateSaveStrategy sceneStateSaveStrategy,
                                  boolean supportRestore,
                                  @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = viewFinder.requireViewById(sceneContainerViewId);
        this.onActivityCreated(activity, viewGroup, scene, rootScopeFactory, sceneStateSaveStrategy, supportRestore, savedInstanceState);
    }

    public void onActivityCreated(@NonNull Activity activity,
                                  @NonNull ViewGroup viewGroup,
                                  @NonNull T scene,
                                  @NonNull Scope.RootScopeFactory rootScopeFactory,
                                  @Nullable SceneStateSaveStrategy sceneStateSaveStrategy,
                                  boolean supportRestore,
                                  @Nullable Bundle savedInstanceState) {
        SceneTrace.beginSection(TRACE_ACTIVITY_CREATED_TAG);
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
        this.mSceneStateSaveStrategy = sceneStateSaveStrategy;

        this.mState = SceneLifecycleManagerState.ACTIVITY_CREATED;
        this.mStateSaved = false;
        log("onActivityCreated");

        this.mScene = scene;
        if (!this.mSupportRestore) {
            this.mScene.disableSupportRestore();
        }
        this.mScene.setRootScopeFactory(rootScopeFactory);
        this.mScene.dispatchAttachActivity(activity);
        this.mScene.dispatchAttachScene(null);

        Bundle sceneSavedInstanceState = savedInstanceState;
        if (savedInstanceState != null && this.mSceneStateSaveStrategy != null) {
            sceneSavedInstanceState = mSceneStateSaveStrategy.onRestoreInstanceState(savedInstanceState);
        }
        if (sceneSavedInstanceState != null && !sceneSavedInstanceState.getBoolean(SCENE_LIFECYCLE_MANAGER_ON_SAVE_INSTANCE_STATE_TAG)) {
            throw new OnSaveInstanceStateMethodMissingException("savedInstanceState argument is not null, but previous onSaveInstanceState() is missing");
        }
        this.mScene.dispatchCreate(sceneSavedInstanceState);
        this.mScene.dispatchCreateView(sceneSavedInstanceState, viewGroup);
        viewGroup.addView(this.mScene.requireView(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.mScene.dispatchActivityCreated(sceneSavedInstanceState);
        SceneTrace.endSection();
    }

    public void onStart() {
        SceneTrace.beginSection(TRACE_START_TAG);
        if (this.mState != SceneLifecycleManagerState.ACTIVITY_CREATED && this.mState != SceneLifecycleManagerState.STOP) {
            throw new IllegalStateException("invoke onActivityCreated() or onStop() first, current state " + this.mState.toString());
        }
        this.mState = SceneLifecycleManagerState.START;
        log("onStart");
        this.mScene.dispatchStart();
        this.mStateSaved = false;
        SceneTrace.endSection();
    }

    public void onResume() {
        SceneTrace.beginSection(TRACE_RESUME_TAG);
        if (this.mState != SceneLifecycleManagerState.START && this.mState != SceneLifecycleManagerState.PAUSE) {
            throw new IllegalStateException("invoke onStart() or onPause() first, current state " + this.mState.toString());
        }
        this.mState = SceneLifecycleManagerState.RESUME;
        log("onResume");
        this.mScene.dispatchResume();
        this.mStateSaved = false;
        SceneTrace.endSection();
    }

    public void onPause() {
        SceneTrace.beginSection(TRACE_PAUSE_TAG);
        if (this.mState != SceneLifecycleManagerState.RESUME) {
            throw new IllegalStateException("invoke onResume() first, current state " + this.mState.toString());
        }
        this.mState = SceneLifecycleManagerState.PAUSE;
        log("onPause");
        this.mScene.dispatchPause();
        SceneTrace.endSection();
    }

    public void onStop() {
        SceneTrace.beginSection(TRACE_STOP_TAG);
        if (this.mState != SceneLifecycleManagerState.PAUSE && this.mState != SceneLifecycleManagerState.START) {
            throw new IllegalStateException("invoke onPause() or onStart() first, current state " + this.mState.toString());
        }
        this.mState = SceneLifecycleManagerState.STOP;
        log("onStop");
        this.mScene.dispatchStop();
        SceneTrace.endSection();
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
        SceneTrace.beginSection(TRACE_DESTROY_VIEW_TAG);
        if (this.mState != SceneLifecycleManagerState.STOP && this.mState != SceneLifecycleManagerState.ACTIVITY_CREATED) {
            throw new IllegalStateException("invoke onStop() or onActivityCreated() first, current state " + this.mState.toString());
        }
        this.mState = SceneLifecycleManagerState.NONE;
        log("onDestroyView");
        this.mScene.dispatchDestroyView();
        this.mScene.dispatchDestroy();
        this.mScene.dispatchDetachScene();
        Activity activity = this.mScene.requireActivity();
        this.mScene.dispatchDetachActivity();
        this.mScene.setRootScopeFactory(null);
        if (this.mSceneStateSaveStrategy != null) {
            if (this.mSupportRestore) {
                if (!this.mStateSaved) {
                    this.mSceneStateSaveStrategy.onClear();
                } else if (activity.isFinishing()) {
                    this.mSceneStateSaveStrategy.onClear();
                }
            }
            this.mSceneStateSaveStrategy = null;
        }
        this.mScene = null;
        SceneTrace.endSection();
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        SceneTrace.beginSection(TRACE_SAVE_INSTANCE_STATE_TAG);
        Utility.requireNonNull(outState, "outState can't be null");
        if (this.mState == SceneLifecycleManagerState.NONE) {
            throw new IllegalStateException("invoke onActivityCreated() first, current state " + this.mState.toString());
        }
        if (!this.mSupportRestore) {
            throw new IllegalArgumentException("cant invoke onSaveInstanceState when not support restore");
        }
        log("onSaveInstanceState");

        int saveStateReason = outState.getInt(SceneStateSaveReason.KEY_SCENE_SAVE_STATE_REASON, SceneStateSaveReason.PARENT_SAVED);

        if (this.mSceneStateSaveStrategy != null) {
            Bundle sceneOutState = new Bundle();
            sceneOutState.putInt(SceneStateSaveReason.KEY_SCENE_SAVE_STATE_REASON, saveStateReason);
            this.mScene.dispatchSaveInstanceState(sceneOutState);
            sceneOutState.putBoolean(SCENE_LIFECYCLE_MANAGER_ON_SAVE_INSTANCE_STATE_TAG, true);
            this.mSceneStateSaveStrategy.onSaveInstanceState(outState, sceneOutState);
        } else {
            outState.putInt(SceneStateSaveReason.KEY_SCENE_SAVE_STATE_REASON, saveStateReason);
            this.mScene.dispatchSaveInstanceState(outState);
            outState.putBoolean(SCENE_LIFECYCLE_MANAGER_ON_SAVE_INSTANCE_STATE_TAG, true);
        }
        this.mStateSaved = true;
        SceneTrace.endSection();
    }

    private void log(@NonNull String log) {
        if (DEBUG) {
            Log.d(TAG + "#" + hashCode(), log);
        }
    }
}
