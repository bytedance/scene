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
import com.bytedance.scene.navigation.NavigationScene;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * TODO merge SceneLifecycleDispatcher SceneLifecycleManager?
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class SceneLifecycleDispatcher implements SceneContainerLifecycleCallback {
    private static final String TAG = "SCENE";
    @IdRes
    private final int mSceneContainerViewId;
    private final ViewFinder mViewFinder;
    private final NavigationScene mScene;
    private final Scope.RootScopeFactory mRootScopeFactory;
    private final boolean mSupportRestore;
    private final SceneComponentFactory mRootSceneComponentFactory;
    private final SceneLifecycleManager mLifecycleManager = new SceneLifecycleManager();

    public SceneLifecycleDispatcher(@IdRes int sceneContainerViewId,
                                    ViewFinder viewFinder,
                                    NavigationScene rootScene,
                                    Scope.RootScopeFactory rootScopeFactory,
                                    SceneComponentFactory sceneComponentFactory,
                                    boolean supportRestore) {
        this.mSceneContainerViewId = sceneContainerViewId;
        this.mViewFinder = viewFinder;
        this.mScene = rootScene;
        this.mRootScopeFactory = rootScopeFactory;
        this.mRootSceneComponentFactory = sceneComponentFactory;
        this.mSupportRestore = supportRestore;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = this.mViewFinder.requireViewById(this.mSceneContainerViewId);
        this.mLifecycleManager.onActivityCreated(activity, viewGroup,
                this.mScene, this.mRootScopeFactory,
                this.mRootSceneComponentFactory, this.mSupportRestore, this.mSupportRestore ? savedInstanceState : null);
    }

    @Override
    public void onStarted() {
        this.mLifecycleManager.onStart();
    }

    @Override
    public void onResumed() {
        this.mLifecycleManager.onResume();
    }

    @Override
    public void onPaused() {
        this.mLifecycleManager.onPause();
    }

    @Override
    public void onStopped() {
        this.mLifecycleManager.onStop();
    }

    @Override
    public void onViewDestroyed() {
        this.mLifecycleManager.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (this.mSupportRestore) {
            outState.putString(TAG, this.mScene.getClass().getName());
            this.mLifecycleManager.onSaveInstanceState(outState);
        }
    }
}
