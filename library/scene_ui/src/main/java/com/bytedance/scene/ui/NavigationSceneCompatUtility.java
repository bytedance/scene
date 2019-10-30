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
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.bytedance.scene.*;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scene.utlity.ThreadUtility;
import com.bytedance.scene.utlity.Utility;

import java.util.HashSet;
import java.util.WeakHashMap;

/**
 * Created by JiangQi on 9/4/18.
 */
public final class NavigationSceneCompatUtility {
    private static final String LIFE_CYCLE_FRAGMENT_TAG = "LifeCycleCompatFragment";
    private static final WeakHashMap<Fragment, HashSet<String>> CHECK_DUPLICATE_TAG_MAP = new WeakHashMap<>();

    private NavigationSceneCompatUtility() {
    }

    public static final class Builder {
        @NonNull
        private final Fragment mFragment;
        @NonNull
        private final Class<? extends Scene> mRootSceneClazz;
        @Nullable
        private Bundle mRootSceneArguments;
        private boolean mDrawWindowBackground = true;
        private boolean mFixSceneBackgroundEnabled = true;
        @DrawableRes
        private int mSceneBackgroundResId = 0;
        @IdRes
        private final int mIdRes;
        private boolean mSupportRestore = false;
        @Nullable
        private SceneComponentFactory mRootSceneComponentFactory;
        @NonNull
        private String mTag = LIFE_CYCLE_FRAGMENT_TAG;
        private boolean mImmediate = true;

        private Builder(@NonNull Fragment fragment, @NonNull Class<? extends Scene> rootSceneClazz, @IdRes int containerId) {
            this.mFragment = Utility.requireNonNull(fragment, "Fragment can't be null");
            this.mRootSceneClazz = Utility.requireNonNull(rootSceneClazz, "Root Scene class can't be null");
            this.mIdRes = containerId;
        }

        @NonNull
        public Builder rootSceneArguments(@Nullable Bundle rootSceneArguments) {
            this.mRootSceneArguments = rootSceneArguments;
            return this;
        }

        @NonNull
        public Builder supportRestore(boolean supportRestore) {
            this.mSupportRestore = supportRestore;
            return this;
        }

        @NonNull
        public Builder rootSceneComponentFactory(@Nullable SceneComponentFactory rootSceneComponentFactory) {
            this.mRootSceneComponentFactory = rootSceneComponentFactory;
            return this;
        }

        @NonNull
        public Builder drawWindowBackground(boolean drawWindowBackground) {
            this.mDrawWindowBackground = drawWindowBackground;
            return this;
        }

        @NonNull
        public Builder fixSceneWindowBackgroundEnabled(boolean fixSceneBackground) {
            this.mFixSceneBackgroundEnabled = fixSceneBackground;
            return this;
        }

        @NonNull
        public Builder sceneBackground(@DrawableRes int resId) {
            this.mSceneBackgroundResId = resId;
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
        public SceneDelegate build() {
            NavigationSceneOptions navigationSceneOptions = new NavigationSceneOptions(this.mRootSceneClazz, this.mRootSceneArguments);
            navigationSceneOptions.setDrawWindowBackground(this.mDrawWindowBackground);
            navigationSceneOptions.setFixSceneWindowBackgroundEnabled(this.mFixSceneBackgroundEnabled);
            navigationSceneOptions.setSceneBackground(this.mSceneBackgroundResId);
            return setupWithFragment(this.mFragment, this.mIdRes, navigationSceneOptions, this.mRootSceneComponentFactory, this.mSupportRestore, this.mTag, this.mImmediate);
        }
    }

    @NonNull
    public static Builder setupWithFragment(@NonNull final Fragment fragment,
                                            @NonNull Class<? extends Scene> rootScene,
                                            @IdRes int containerId) {
        return new Builder(fragment, rootScene, containerId);
    }

    /**
     * use {@link #setupWithFragment(Fragment, Class, int)} instead
     */
    @Deprecated
    @NonNull
    public static SceneDelegate setupWithFragment(@NonNull final Fragment fragment,
                                                  @IdRes int containerId,
                                                  @Nullable Bundle savedInstanceState,
                                                  @NonNull Class<? extends Scene> rootScene,
                                                  @Nullable Bundle bundle) {
        return setupWithFragment(fragment, containerId, savedInstanceState,
                new NavigationSceneOptions(rootScene, bundle),
                null,
                false);
    }

    /**
     * use {@link #setupWithFragment(Fragment, Class, int)} instead
     */
    @Deprecated
    @NonNull
    public static SceneDelegate setupWithFragment(@NonNull final Fragment fragment,
                                                  @IdRes int containerId,
                                                  @Nullable Bundle savedInstanceState,
                                                  @NonNull Class<? extends Scene> rootScene,
                                                  @Nullable Bundle bundle,
                                                  boolean supportRestore) {
        return setupWithFragment(fragment, containerId, savedInstanceState,
                new NavigationSceneOptions(rootScene, bundle),
                null,
                supportRestore);
    }

    /**
     * use {@link #setupWithFragment(Fragment, Class, int)} instead
     */
    @Deprecated
    @NonNull
    public static SceneDelegate setupWithFragment(@NonNull final Fragment fragment,
                                                  @IdRes int containerId,
                                                  @Nullable Bundle savedInstanceState,
                                                  @NonNull Class<? extends Scene> rootScene,
                                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                  @Nullable Bundle bundle,
                                                  boolean supportRestore) {
        return setupWithFragment(fragment, containerId, savedInstanceState,
                new NavigationSceneOptions(rootScene, bundle),
                rootSceneComponentFactory,
                supportRestore);
    }

    /**
     * use {@link #setupWithFragment(Fragment, Class, int)} instead
     */
    @Deprecated
    @NonNull
    public static SceneDelegate setupWithFragment(@NonNull final Fragment fragment,
                                                  @IdRes int containerId,
                                                  @Nullable Bundle savedInstanceState,
                                                  @NonNull NavigationSceneOptions navigationSceneOptions,
                                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                  final boolean supportRestore) {
        return setupWithFragment(fragment, containerId, savedInstanceState, navigationSceneOptions,
                rootSceneComponentFactory, supportRestore, LIFE_CYCLE_FRAGMENT_TAG, true);
    }

    /**
     * use {@link #setupWithFragment(Fragment, Class, int)} instead
     */
    @Deprecated
    @NonNull
    public static SceneDelegate setupWithFragment(@NonNull final Fragment fragment,
                                                  @IdRes int containerId,
                                                  @Nullable Bundle savedInstanceState,
                                                  @NonNull NavigationSceneOptions navigationSceneOptions,
                                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                  final boolean supportRestore,
                                                  @NonNull final String tag,
                                                  final boolean immediate) {
        return setupWithFragment(fragment, containerId, navigationSceneOptions, rootSceneComponentFactory, supportRestore, tag, immediate);
    }

    @NonNull
    private static SceneDelegate setupWithFragment(@NonNull final Fragment fragment,
                                                   @IdRes int containerId,
                                                   @NonNull NavigationSceneOptions navigationSceneOptions,
                                                   @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                   final boolean supportRestore,
                                                   @NonNull final String tag,
                                                   final boolean immediate) {
        ThreadUtility.checkUIThread();
        if (tag == null) {
            throw new IllegalArgumentException("tag cant be null");
        }
        checkDuplicateTag(fragment, tag);

        final FragmentManager fragmentManager = fragment.getChildFragmentManager();
        LifeCycleCompatFragment lifeCycleFragment = (LifeCycleCompatFragment) fragmentManager.findFragmentByTag(tag);
        if (lifeCycleFragment != null && !supportRestore) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(lifeCycleFragment);
            FragmentUtility.commitFragment(transaction, immediate);
            lifeCycleFragment = null;
        }

        ViewFinder viewFinder = new FragmentViewFinder(fragment);
        final NavigationScene navigationScene = (NavigationScene) SceneInstanceUtility.getInstanceFromClass(NavigationScene.class,
                navigationSceneOptions.toBundle());
        ScopeHolderCompatFragment targetScopeHolderFragment = null;
        SceneLifecycleDispatcher dispatcher = null;
        if (lifeCycleFragment != null) {
            final ScopeHolderCompatFragment scopeHolderFragment = ScopeHolderCompatFragment.install(fragment, tag, false, immediate);
            targetScopeHolderFragment = scopeHolderFragment;

            dispatcher = new SceneLifecycleDispatcher(containerId, viewFinder, navigationScene, scopeHolderFragment, rootSceneComponentFactory, supportRestore);
            lifeCycleFragment.setSceneContainerLifecycleCallback(dispatcher);
        } else {
            final ScopeHolderCompatFragment scopeHolderFragment = ScopeHolderCompatFragment.install(fragment, tag, !supportRestore, immediate);
            lifeCycleFragment = LifeCycleCompatFragment.newInstance(supportRestore);

            dispatcher = new SceneLifecycleDispatcher(containerId, viewFinder, navigationScene, scopeHolderFragment, rootSceneComponentFactory, supportRestore);
            lifeCycleFragment.setSceneContainerLifecycleCallback(dispatcher);

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(containerId, lifeCycleFragment, tag);
            FragmentUtility.commitFragment(transaction, immediate);
            targetScopeHolderFragment = scopeHolderFragment;
        }

        final LifeCycleCompatFragment finalLifeCycleFragment = lifeCycleFragment;
        final ScopeHolderCompatFragment finalTargetScopeHolderFragment = targetScopeHolderFragment;
        final SceneLifecycleDispatcher finalDispatcher = dispatcher;
        final SceneDelegate proxy = new SceneDelegate() {
            private boolean mAbandoned = false;

            @Override
            public boolean onBackPressed() {
                return !mAbandoned && navigationScene.onBackPressed();
            }

            @Override
            @Nullable
            public NavigationScene getNavigationScene() {
                if (this.mAbandoned) {
                    return null;
                }
                return navigationScene;
            }

            @Override
            public void setNavigationSceneAvailableCallback(@NonNull NavigationSceneAvailableCallback callback) {
                callback.onNavigationSceneAvailable(navigationScene);
            }

            @Override
            public void abandon() {
                if (this.mAbandoned) {
                    return;
                }
                this.mAbandoned = true;
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
                            CHECK_DUPLICATE_TAG_MAP.get(fragment).remove(tag);
                        }
                    }, false);
                    FragmentUtility.commitFragment(transaction, true);
                } else {
                    FragmentUtility.commitFragment(transaction, false);
                    CHECK_DUPLICATE_TAG_MAP.get(fragment).remove(tag);
                }
            }
        };
        return proxy;
    }

    private static void checkDuplicateTag(@NonNull Fragment fragment, @NonNull String tag) {
        if (CHECK_DUPLICATE_TAG_MAP.get(fragment) != null && CHECK_DUPLICATE_TAG_MAP.get(fragment).contains(tag)) {
            throw new IllegalArgumentException("tag duplicate, use another tag when invoke setupWithActivity for the second time in same Fragment");
        } else {
            HashSet<String> set = CHECK_DUPLICATE_TAG_MAP.get(fragment);
            if (set == null) {
                set = new HashSet<>();
                CHECK_DUPLICATE_TAG_MAP.put(fragment, set);
            }
            set.add(tag);
        }
    }
}
