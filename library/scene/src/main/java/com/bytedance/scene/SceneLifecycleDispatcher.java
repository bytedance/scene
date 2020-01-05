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
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.view.ViewGroup;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * TODO merge SceneLifecycleDispatcher SceneLifecycleManager?
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class SceneLifecycleDispatcher<T extends Scene & SceneParent> implements SceneContainerLifecycleCallback {
    private static final String TAG = "SCENE";

    private static final String TRACE_ACTIVITY_CREATED_TAG = "SceneLifecycleDispatcher#OnActivityCreated";
    private static final String TRACE_START_TAG = "SceneLifecycleDispatcher#OnStart";
    private static final String TRACE_RESUME_TAG = "SceneLifecycleDispatcher#OnResume";
    private static final String TRACE_PAUSE_TAG = "SceneLifecycleDispatcher#OnPause";
    private static final String TRACE_STOP_TAG = "SceneLifecycleDispatcher#OnStop";
    private static final String TRACE_DESTROY_VIEW_TAG = "SceneLifecycleDispatcher#OnDestroyView";
    private static final String TRACE_SAVE_INSTANCE_STATE_TAG = "SceneLifecycleDispatcher#OnSaveInstance";

    @IdRes
    private final int mSceneContainerViewId;
    private final ViewFinder mViewFinder;
    private final T mScene;
    private final Scope.RootScopeFactory mRootScopeFactory;
    private final boolean mSupportRestore;
    private final SceneLifecycleManager<T> mLifecycleManager = new SceneLifecycleManager<>();

    public SceneLifecycleDispatcher(@IdRes int sceneContainerViewId,
                                    ViewFinder viewFinder,
                                    T rootScene,
                                    Scope.RootScopeFactory rootScopeFactory,
                                    boolean supportRestore) {
        this.mSceneContainerViewId = sceneContainerViewId;
        this.mViewFinder = viewFinder;
        this.mScene = rootScene;
        this.mRootScopeFactory = rootScopeFactory;
        this.mSupportRestore = supportRestore;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        SceneTrace.beginSection(TRACE_ACTIVITY_CREATED_TAG);
        ViewGroup viewGroup = this.mViewFinder.requireViewById(this.mSceneContainerViewId);
        this.mLifecycleManager.onActivityCreated(activity, viewGroup, this.mScene, this.mRootScopeFactory,
                this.mSupportRestore, this.mSupportRestore ? savedInstanceState : null);
        SceneTrace.endSection();
    }

    @Override
    public void onStarted() {
        SceneTrace.beginSection(TRACE_START_TAG);
        this.mLifecycleManager.onStart();
        SceneTrace.endSection();
    }

    @Override
    public void onResumed() {
        SceneTrace.beginSection(TRACE_RESUME_TAG);
        this.mLifecycleManager.onResume();
        SceneTrace.endSection();
    }

    @Override
    public void onPaused() {
        SceneTrace.beginSection(TRACE_PAUSE_TAG);
        this.mLifecycleManager.onPause();
        SceneTrace.endSection();
    }

    @Override
    public void onStopped() {
        SceneTrace.beginSection(TRACE_STOP_TAG);
        this.mLifecycleManager.onStop();
        SceneTrace.endSection();
    }

    @Override
    public void onViewDestroyed() {
        SceneTrace.beginSection(TRACE_DESTROY_VIEW_TAG);
        this.mLifecycleManager.onDestroyView();
        SceneTrace.endSection();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (this.mSupportRestore) {
            outState.putString(TAG, this.mScene.getClass().getName());
            SceneTrace.beginSection(TRACE_SAVE_INSTANCE_STATE_TAG);
            this.mLifecycleManager.onSaveInstanceState(outState);
            SceneTrace.endSection();
        }
    }
}
