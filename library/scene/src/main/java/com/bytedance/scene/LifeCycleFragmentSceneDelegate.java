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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.annotation.NonNull;

import android.support.annotation.Nullable;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.Utility;

/**
 * Created by JiangQi on 9/11/18.
 */
final class LifeCycleFragmentSceneDelegate implements SceneDelegate, NavigationSceneAvailableCallback {
    private final Activity mActivity;
    private final LifeCycleFragment mLifeCycleFragment;
    private final ScopeHolderFragment mScopeHolderFragment;
    private final SceneLifecycleDispatcher mSceneLifecycleDispatcher;
    private final Boolean mImmediate;

    private NavigationScene mNavigationScene;
    private NavigationSceneAvailableCallback mNavigationSceneAvailableCallback;
    private boolean mAbandoned = false;

    LifeCycleFragmentSceneDelegate(@NonNull Activity activity, @NonNull LifeCycleFragment lifeCycleFragment, @NonNull ScopeHolderFragment scopeHolderFragment,
                                   @NonNull SceneLifecycleDispatcher dispatcher,
                                   boolean immediate) {
        this.mActivity = activity;
        this.mLifeCycleFragment = lifeCycleFragment;
        this.mScopeHolderFragment = scopeHolderFragment;
        this.mSceneLifecycleDispatcher = dispatcher;
        this.mImmediate = immediate;
    }

    @Override
    public boolean onBackPressed() {
        NavigationScene navigationScene = mSceneLifecycleDispatcher.getNavigationScene();
        return !this.mAbandoned && navigationScene != null && navigationScene.onBackPressed();
    }

    @Override
    @Nullable
    public NavigationScene getNavigationScene() {
        if (this.mAbandoned) {
            return null;
        }
        return mSceneLifecycleDispatcher.getNavigationScene();
    }

    @Override
    public final void onNavigationSceneAvailable(@NonNull NavigationScene navigationScene) {
        this.mNavigationScene = navigationScene;
        if (this.mNavigationSceneAvailableCallback != null) {
            this.mNavigationSceneAvailableCallback.onNavigationSceneAvailable(navigationScene);
        }
    }

    @Override
    public final void setNavigationSceneAvailableCallback(@NonNull NavigationSceneAvailableCallback callback) {
        this.mNavigationSceneAvailableCallback = callback;
        if (this.mNavigationScene != null) {
            this.mNavigationSceneAvailableCallback.onNavigationSceneAvailable(this.mNavigationScene);
        }
    }

    @Override
    public void abandon() {
        if (this.mAbandoned) {
            return;
        }
        this.mAbandoned = true;
        FragmentManager fragmentManager = mActivity.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().remove(this.mLifeCycleFragment).remove(this.mScopeHolderFragment);
        if (this.mImmediate) {
            this.mLifeCycleFragment.setLifecycleFragmentDetachCallback(new LifeCycleFragment.LifecycleFragmentDetachCallback() {
                @Override
                public void onDetach() {
                    NavigationSceneUtility.removeTag(mActivity, mLifeCycleFragment.getTag());
                }
            });
            Utility.commitFragment(fragmentManager, fragmentTransaction, true);
        } else {
            Utility.commitFragment(fragmentManager, fragmentTransaction, false);
            NavigationSceneUtility.removeTag(mActivity, mLifeCycleFragment.getTag());
        }
    }
}
