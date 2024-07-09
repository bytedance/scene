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
import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scene.utlity.ThreadUtility;
import com.bytedance.scene.utlity.Utility;

import java.util.HashSet;
import java.util.WeakHashMap;

public final class GroupSceneUtility {
    static final String LIFE_CYCLE_FRAGMENT_TAG = "LifeCycleFragment";
    private static final WeakHashMap<Activity, HashSet<String>> CHECK_DUPLICATE_TAG_MAP = new WeakHashMap<>();

    private GroupSceneUtility() {
    }

    public static final class Builder {
        @NonNull
        private final Activity mActivity;
        @NonNull
        private final Class<? extends GroupScene> mRootSceneClazz;
        @Nullable
        private Bundle mRootSceneArguments;
        @IdRes
        private int mIdRes = android.R.id.content;
        private boolean mSupportRestore = false;
        @Nullable
        private SceneComponentFactory mRootSceneComponentFactory;
        @NonNull
        private String mTag = LIFE_CYCLE_FRAGMENT_TAG;
        private boolean mImmediate = true;

        private Builder(@NonNull Activity activity, @NonNull Class<? extends GroupScene> rootSceneClazz) {
            this.mActivity = Utility.requireNonNull(activity, "Activity can't be null");
            this.mRootSceneClazz = Utility.requireNonNull(rootSceneClazz, "Root Scene class can't be null");
        }

        @NonNull
        public Builder rootSceneArguments(@Nullable Bundle rootSceneArguments) {
            this.mRootSceneArguments = rootSceneArguments;
            return this;
        }

        @NonNull
        public Builder rootSceneComponentFactory(@Nullable SceneComponentFactory rootSceneComponentFactory) {
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
        public Builder toView(@IdRes int idRes) {
            this.mIdRes = idRes;
            return this;
        }

        @NonNull
        public Builder supportRestore(boolean supportRestore) {
            this.mSupportRestore = supportRestore;
            return this;
        }

        @NonNull
        public Builder tag(@NonNull String tag) {
            this.mTag = Utility.requireNonNull(tag, "Tag can't be null");
            return this;
        }

        @NonNull
        public Builder immediate(boolean immediate) {
            this.mImmediate = immediate;
            return this;
        }

        @NonNull
        public GroupSceneDelegate build() {
            return setupWithActivity(this.mActivity, this.mIdRes, this.mRootSceneClazz, this.mRootSceneArguments, this.mRootSceneComponentFactory, this.mSupportRestore, this.mTag, this.mImmediate);
        }
    }

    @NonNull
    public static Builder setupWithActivity(@NonNull final Activity activity,
                                            @NonNull Class<? extends GroupScene> rootScene) {
        return new Builder(activity, rootScene);
    }

    @NonNull
    private static GroupSceneDelegate setupWithActivity(@NonNull final Activity activity,
                                                        @IdRes int idRes,
                                                        @NonNull Class<? extends GroupScene> rootSceneClazz,
                                                        @Nullable Bundle arguments,
                                                        @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                        final boolean supportRestore,
                                                        @NonNull String tag,
                                                        final boolean immediate) {
        ThreadUtility.checkUIThread();
        if (tag == null) {
            throw new IllegalArgumentException("tag cant be null");
        }
        checkDuplicateTag(activity, tag);

        GroupScene groupScene = null;
        if (rootSceneComponentFactory != null) {
            groupScene = (GroupScene) rootSceneComponentFactory.instantiateScene(activity.getClass().getClassLoader(), rootSceneClazz.getName(), arguments);
        }
        if (groupScene == null) {
            groupScene = (GroupScene) SceneInstanceUtility.getInstanceFromClass(rootSceneClazz, arguments);
        }

        if (!Utility.isActivityStatusValid(activity)) {
            return new DestroyedGroupSceneDelegate(groupScene);
        }

        FragmentManager fragmentManager = activity.getFragmentManager();
        LifeCycleFragment lifeCycleFragment = (LifeCycleFragment) fragmentManager.findFragmentByTag(tag);
        if (lifeCycleFragment != null && !supportRestore) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(lifeCycleFragment);
            Utility.commitFragment(fragmentManager, transaction, immediate);
            lifeCycleFragment = null;
        }

        ViewFinder viewFinder = new ActivityViewFinder(activity);

        ScopeHolderFragment targetScopeHolderFragment = null;
        SceneLifecycleDispatcher<GroupScene> dispatcher = null;
        if (lifeCycleFragment != null) {
            final ScopeHolderFragment scopeHolderFragment = ScopeHolderFragment.install(activity, tag, false, immediate);
            dispatcher = new SceneLifecycleDispatcher<>(idRes, viewFinder, groupScene, scopeHolderFragment, supportRestore);
            lifeCycleFragment.setSceneContainerLifecycleCallback(dispatcher);
            targetScopeHolderFragment = scopeHolderFragment;
        } else {
            lifeCycleFragment = LifeCycleFragment.newInstance(supportRestore);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(idRes, lifeCycleFragment, tag);

            final ScopeHolderFragment scopeHolderFragment = ScopeHolderFragment.install(activity, tag, !supportRestore, immediate);
            dispatcher = new SceneLifecycleDispatcher<>(idRes, viewFinder, groupScene, scopeHolderFragment, supportRestore);
            lifeCycleFragment.setSceneContainerLifecycleCallback(dispatcher);

            Utility.commitFragment(fragmentManager, transaction, immediate);
            targetScopeHolderFragment = scopeHolderFragment;
        }
        final GroupScene finalGroupScene = groupScene;
        final LifeCycleFragment finalLifeCycleFragment = lifeCycleFragment;
        final ScopeHolderFragment finalTargetScopeHolderFragment = targetScopeHolderFragment;
        return new GroupSceneDelegate() {
            boolean mAbandoned;

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
                FragmentManager fragmentManager = activity.getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().remove(finalLifeCycleFragment).remove(finalTargetScopeHolderFragment);
                if (immediate) {
                    finalLifeCycleFragment.setLifecycleFragmentDetachCallback(new LifeCycleFragment.LifecycleFragmentDetachCallback() {
                        @Override
                        public void onDetach() {
                            removeTag(activity, finalLifeCycleFragment.getTag());
                            if (view != null) {
                                Utility.removeFromParentView(view);
                            }
                        }
                    });
                    Utility.commitFragment(fragmentManager, fragmentTransaction, true);
                } else {
                    Utility.commitFragment(fragmentManager, fragmentTransaction, false);
                    removeTag(activity, finalLifeCycleFragment.getTag());
                    if (view != null) {
                        Utility.removeFromParentView(view);
                    }
                }
            }
        };
    }

    static void checkDuplicateTag(@NonNull Activity activity, @NonNull String tag) {
        if (CHECK_DUPLICATE_TAG_MAP.get(activity) != null && CHECK_DUPLICATE_TAG_MAP.get(activity).contains(tag)) {
            throw new IllegalArgumentException("tag duplicate, use another tag when invoke setupWithActivity for the second time in same Activity");
        } else {
            HashSet<String> set = CHECK_DUPLICATE_TAG_MAP.get(activity);
            if (set == null) {
                set = new HashSet<>();
                CHECK_DUPLICATE_TAG_MAP.put(activity, set);
            }
            set.add(tag);
        }
    }

    static void removeTag(@NonNull Activity activity, @NonNull String tag) {
        CHECK_DUPLICATE_TAG_MAP.get(activity).remove(tag);
    }
}