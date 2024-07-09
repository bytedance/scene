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
package com.bytedance.scene.ui;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.bytedance.scene.*;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scene.utlity.ThreadUtility;
import com.bytedance.scene.utlity.Utility;

import java.util.HashSet;
import java.util.WeakHashMap;

public class GroupSceneCompatUtility {
    private GroupSceneCompatUtility() {
    }

    public static final class Builder {
        @NonNull
        private final Fragment mFragment;
        @NonNull
        private final Class<? extends GroupScene> mRootSceneClazz;
        @Nullable
        private Bundle mRootSceneArguments;
        @IdRes
        private final int mIdRes;
        private boolean mSupportRestore = false;
        @Nullable
        private SceneComponentFactory mRootSceneComponentFactory;
        @NonNull
        private String mTag = NavigationSceneCompatUtility.LIFE_CYCLE_FRAGMENT_TAG;
        private boolean mImmediate = true;

        private Builder(@NonNull Fragment fragment, @NonNull Class<? extends GroupScene> rootSceneClazz, @IdRes int containerId) {
            this.mFragment = Utility.requireNonNull(fragment, "Fragment can't be null");
            this.mRootSceneClazz = Utility.requireNonNull(rootSceneClazz, "Root Scene class can't be null");
            this.mIdRes = containerId;
        }

        @NonNull
        public GroupSceneCompatUtility.Builder rootSceneArguments(@Nullable Bundle rootSceneArguments) {
            this.mRootSceneArguments = rootSceneArguments;
            return this;
        }

        @NonNull
        public GroupSceneCompatUtility.Builder supportRestore(boolean supportRestore) {
            this.mSupportRestore = supportRestore;
            return this;
        }

        @NonNull
        public GroupSceneCompatUtility.Builder rootSceneComponentFactory(@Nullable SceneComponentFactory rootSceneComponentFactory) {
            this.mRootSceneComponentFactory = rootSceneComponentFactory;
            return this;
        }

        @NonNull
        public Builder rootScene(@NonNull final GroupScene scene) {
            if (scene.getClass() != this.mRootSceneClazz) {
                throw new IllegalArgumentException("Scene type error, must be " + this.mRootSceneClazz + " instance");
            }

            this.mRootSceneComponentFactory = new SceneComponentFactory() {
                @Nullable
                @Override
                public Scene instantiateScene(@NonNull ClassLoader cl, @NonNull String className, @Nullable Bundle bundle) {
                    return scene;
                }
            };
            return this;
        }

        @NonNull
        public GroupSceneCompatUtility.Builder tag(@NonNull String tag) {
            this.mTag = Utility.requireNonNull(tag, "Tag can't be null");
            return this;
        }

        @NonNull
        public GroupSceneCompatUtility.Builder immediate(boolean immediate) {
            this.mImmediate = immediate;
            return this;
        }

        @NonNull
        public GroupSceneDelegate build() {
            return setupWithFragment(this.mFragment, this.mIdRes, this.mRootSceneClazz, this.mRootSceneArguments, this.mRootSceneComponentFactory, this.mSupportRestore, this.mTag, this.mImmediate);
        }
    }

    @NonNull
    public static GroupSceneCompatUtility.Builder setupWithFragment(@NonNull final Fragment fragment,
                                                                    @NonNull Class<? extends GroupScene> rootScene,
                                                                    @IdRes int containerId) {
        return new GroupSceneCompatUtility.Builder(fragment, rootScene, containerId);
    }

    @NonNull
    private static GroupSceneDelegate setupWithFragment(@NonNull final Fragment fragment,
                                                        @IdRes int containerId,
                                                        @NonNull Class<? extends Scene> sceneClazz,
                                                        @Nullable Bundle sceneArguments,
                                                        @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                        final boolean supportRestore,
                                                        @NonNull final String tag,
                                                        final boolean immediate) {
        ThreadUtility.checkUIThread();
        if (tag == null) {
            throw new IllegalArgumentException("tag cant be null");
        }
        NavigationSceneCompatUtility.checkDuplicateTag(fragment, tag);

        final FragmentManager fragmentManager = fragment.getChildFragmentManager();
        LifeCycleCompatFragment lifeCycleFragment = (LifeCycleCompatFragment) fragmentManager.findFragmentByTag(tag);
        if (lifeCycleFragment != null && !supportRestore) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(lifeCycleFragment);
            FragmentUtility.commitFragment(transaction, immediate);
            lifeCycleFragment = null;
        }

        ViewFinder viewFinder = new FragmentViewFinder(fragment);
        GroupScene groupScene = null;
        if (rootSceneComponentFactory != null) {
            groupScene = (GroupScene) rootSceneComponentFactory.instantiateScene(fragment.getClass().getClassLoader(), sceneClazz.getName(), sceneArguments);
        }
        if (groupScene == null) {
            groupScene = (GroupScene) SceneInstanceUtility.getInstanceFromClass(sceneClazz, sceneArguments);
        }

        ScopeHolderCompatFragment targetScopeHolderFragment = null;
        SceneLifecycleDispatcher<GroupScene> dispatcher = null;
        if (lifeCycleFragment != null) {
            final ScopeHolderCompatFragment scopeHolderFragment = ScopeHolderCompatFragment.install(fragment, tag, false, immediate);
            targetScopeHolderFragment = scopeHolderFragment;

            dispatcher = new SceneLifecycleDispatcher<>(containerId, viewFinder, groupScene, scopeHolderFragment, supportRestore);
            lifeCycleFragment.setSceneContainerLifecycleCallback(dispatcher);
        } else {
            final ScopeHolderCompatFragment scopeHolderFragment = ScopeHolderCompatFragment.install(fragment, tag, !supportRestore, immediate);
            lifeCycleFragment = LifeCycleCompatFragment.newInstance(supportRestore);

            dispatcher = new SceneLifecycleDispatcher<>(containerId, viewFinder, groupScene, scopeHolderFragment, supportRestore);
            lifeCycleFragment.setSceneContainerLifecycleCallback(dispatcher);

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(containerId, lifeCycleFragment, tag);
            FragmentUtility.commitFragment(transaction, immediate);
            targetScopeHolderFragment = scopeHolderFragment;
        }

        final LifeCycleCompatFragment finalLifeCycleFragment = lifeCycleFragment;
        final ScopeHolderCompatFragment finalTargetScopeHolderFragment = targetScopeHolderFragment;
        final GroupScene finalGroupScene = groupScene;
        final GroupSceneDelegate proxy = new GroupSceneDelegate() {
            private boolean mAbandoned = false;

            @Nullable
            @Override
            public GroupScene getGroupScene() {
                if (this.mAbandoned) {
                    return null;
                }
                return finalGroupScene;
            }

            @Override
            public void abandon() {
                if (this.mAbandoned) {
                    return;
                }
                this.mAbandoned = true;
                final View view = finalGroupScene.getView();
                FragmentTransaction transaction = fragmentManager.beginTransaction().remove(finalLifeCycleFragment).remove(finalTargetScopeHolderFragment);
                if (immediate) {
                    fragmentManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                        @Override
                        public void onFragmentDetached(FragmentManager fm, Fragment f) {
                            super.onFragmentDetached(fm, f);
                            if (f != finalLifeCycleFragment) {
                                return;
                            }
                            fragmentManager.unregisterFragmentLifecycleCallbacks(this);
                            NavigationSceneCompatUtility.removeTag(fragment, tag);
                        }
                    }, false);
                    FragmentUtility.commitFragment(transaction, true);
                    if (view != null) {
                        Utility.removeFromParentView(view);
                    }
                } else {
                    FragmentUtility.commitFragment(transaction, false);
                    NavigationSceneCompatUtility.removeTag(fragment, tag);
                    if (view != null) {
                        Utility.removeFromParentView(view);
                    }
                }
            }
        };
        return proxy;
    }
}
