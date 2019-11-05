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
import android.view.View;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.Utility;

/**
 * Created by JiangQi on 9/11/18.
 */
final class LifeCycleFragmentSceneDelegate implements SceneDelegate {
    private final Activity mActivity;
    private final NavigationScene mNavigationScene;
    private final LifeCycleFragment mLifeCycleFragment;
    private final ScopeHolderFragment mScopeHolderFragment;
    private final Boolean mImmediate;

    private boolean mAbandoned = false;

    LifeCycleFragmentSceneDelegate(@NonNull Activity activity, @NonNull NavigationScene navigationScene,
                                   @NonNull LifeCycleFragment lifeCycleFragment,
                                   @NonNull ScopeHolderFragment scopeHolderFragment,
                                   boolean immediate) {
        this.mActivity = activity;
        this.mNavigationScene = navigationScene;
        this.mLifeCycleFragment = lifeCycleFragment;
        this.mScopeHolderFragment = scopeHolderFragment;
        this.mImmediate = immediate;
    }

    @Override
    public boolean onBackPressed() {
        return !this.mAbandoned && this.mNavigationScene.onBackPressed();
    }

    @Override
    @Nullable
    public NavigationScene getNavigationScene() {
        if (this.mAbandoned) {
            return null;
        }
        return this.mNavigationScene;
    }

    @Override
    public final void setNavigationSceneAvailableCallback(@NonNull NavigationSceneAvailableCallback callback) {
        callback.onNavigationSceneAvailable(this.mNavigationScene);
    }

    @Override
    public void abandon() {
        if (this.mAbandoned) {
            return;
        }
        this.mAbandoned = true;
        final View view = this.mNavigationScene.getView();
        FragmentManager fragmentManager = mActivity.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().remove(this.mLifeCycleFragment).remove(this.mScopeHolderFragment);
        if (this.mImmediate) {
            this.mLifeCycleFragment.setLifecycleFragmentDetachCallback(new LifeCycleFragment.LifecycleFragmentDetachCallback() {
                @Override
                public void onDetach() {
                    NavigationSceneUtility.removeTag(mActivity, mLifeCycleFragment.getTag());
                    if (view != null) {
                        Utility.removeFromParentView(view);
                    }
                }
            });
            Utility.commitFragment(fragmentManager, fragmentTransaction, true);
        } else {
            Utility.commitFragment(fragmentManager, fragmentTransaction, false);
            NavigationSceneUtility.removeTag(mActivity, mLifeCycleFragment.getTag());
            if (view != null) {
                Utility.removeFromParentView(view);
            }
        }
    }
}
