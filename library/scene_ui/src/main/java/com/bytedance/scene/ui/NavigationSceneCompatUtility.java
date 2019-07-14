package com.bytedance.scene.ui;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.bytedance.scene.SceneDelegate;
import com.bytedance.scene.NavigationSceneAvailableCallback;
import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.Scope;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scene.utlity.ThreadUtility;

import java.util.HashSet;
import java.util.WeakHashMap;

/**
 * Created by JiangQi on 9/4/18.
 */
public class NavigationSceneCompatUtility {
    private static final String LIFE_CYCLE_FRAGMENT_TAG = "LifeCycleCompatFragment";
    private static final WeakHashMap<Fragment, HashSet<String>> CHECK_DUPLICATE_TAG_MAP = new WeakHashMap<>();

    @NonNull
    public static SceneDelegate setupWithFragment(@NonNull final Fragment fragment,
                                                  @IdRes int containerId,
                                                  @Nullable Bundle savedInstanceState,
                                                  @NonNull Class<? extends Scene> rootScene,
                                                  @Nullable Bundle bundle) {
        return setupWithFragment(fragment, containerId, savedInstanceState,
                new NavigationSceneOptions().setRootScene(rootScene, bundle),
                null,
                false);
    }

    @NonNull
    public static SceneDelegate setupWithFragment(@NonNull final Fragment fragment,
                                                  @IdRes int containerId,
                                                  @Nullable Bundle savedInstanceState,
                                                  @NonNull Class<? extends Scene> rootScene,
                                                  @Nullable Bundle bundle,
                                                  boolean supportRestore) {
        return setupWithFragment(fragment, containerId, savedInstanceState,
                new NavigationSceneOptions().setRootScene(rootScene, bundle),
                null,
                supportRestore);
    }

    @NonNull
    public static SceneDelegate setupWithFragment(@NonNull final Fragment fragment,
                                                  @IdRes int containerId,
                                                  @Nullable Bundle savedInstanceState,
                                                  @NonNull Class<? extends Scene> rootScene,
                                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                  @Nullable Bundle bundle,
                                                  boolean supportRestore) {
        return setupWithFragment(fragment, containerId, savedInstanceState,
                new NavigationSceneOptions().setRootScene(rootScene, bundle),
                rootSceneComponentFactory,
                supportRestore);
    }

    @NonNull
    public static SceneDelegate setupWithFragment(@NonNull final Fragment fragment,
                                                  @IdRes int containerId,
                                                  @Nullable Bundle savedInstanceState,
                                                  @NonNull NavigationSceneOptions navigationSceneOptions,
                                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                  final boolean supportRestore) {
        return setupWithFragment(fragment, containerId, savedInstanceState, navigationSceneOptions,
                rootSceneComponentFactory, supportRestore, LIFE_CYCLE_FRAGMENT_TAG);
    }

    @NonNull
    public static SceneDelegate setupWithFragment(@NonNull final Fragment fragment,
                                                  @IdRes int containerId,
                                                  @Nullable Bundle savedInstanceState,
                                                  @NonNull NavigationSceneOptions navigationSceneOptions,
                                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                  final boolean supportRestore,
                                                  @NonNull String tag) {
        ThreadUtility.checkUIThread();
        if (tag == null) {
            throw new IllegalArgumentException("tag cant be null");
        }
        checkDuplicateTag(fragment, tag);

        FragmentManager fragmentManager = fragment.getChildFragmentManager();
        LifeCycleCompatFragment lifeCycleFragment = (LifeCycleCompatFragment) fragmentManager.findFragmentByTag(tag);
        if (lifeCycleFragment != null && !supportRestore) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(lifeCycleFragment);
            transaction.commitNowAllowingStateLoss();
            fragmentManager.executePendingTransactions();
            lifeCycleFragment = null;
        }

        if (lifeCycleFragment != null) {
            final ScopeHolderCompatFragment scopeHolderFragment = ScopeHolderCompatFragment.install(fragment, false);
            lifeCycleFragment.setRootScopeFactory(new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return scopeHolderFragment.getRootScope();
                }
            });
        } else {
            lifeCycleFragment = LifeCycleCompatFragment.newInstance(supportRestore);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(containerId, lifeCycleFragment, tag);
            final NavigationScene navigationScene = (NavigationScene) SceneInstanceUtility.getInstanceFromClass(NavigationScene.class,
                    navigationSceneOptions.toBundle());
            final ScopeHolderCompatFragment scopeHolderFragment = ScopeHolderCompatFragment.install(fragment, !supportRestore);
            lifeCycleFragment.setNavigationScene(navigationScene, new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return scopeHolderFragment.getRootScope();
                }
            });
            transaction.commitNowAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }

        final LifeCycleCompatFragment finalLifeCycleFragment = lifeCycleFragment;
        final FragmentDelegateProxy proxy = new FragmentDelegateProxy() {
            @Override
            public boolean onBackPressed() {
                NavigationScene navigationScene = finalLifeCycleFragment.getNavigationScene();
                return navigationScene != null && navigationScene.onBackPressed();
            }

            @Override
            public NavigationScene getNavigationScene() {
                return finalLifeCycleFragment.getNavigationScene();
            }
        };
        finalLifeCycleFragment.setNavigationSceneAvailableCallback(new NavigationSceneAvailableCallback() {
            @Override
            public void onNavigationSceneAvailable(@NonNull NavigationScene navigationScene) {
                proxy.onNavigationSceneAvailable(navigationScene);
            }
        });
        finalLifeCycleFragment.setRootSceneComponentFactory(rootSceneComponentFactory);
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

    private static abstract class FragmentDelegateProxy implements SceneDelegate, NavigationSceneAvailableCallback {
        @Nullable
        private NavigationScene mNavigationScene;
        @Nullable
        private NavigationSceneAvailableCallback mNavigationSceneAvailableCallback;

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
    }
}
