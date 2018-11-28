package com.bytedance.scene;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scene.utlity.SceneInstanceUtility;

/**
 * Created by JiangQi on 7/30/18.
 */
public class NavigationSceneUtility {
    private static final String LIFE_CYCLE_FRAGMENT_TAG = "LifeCycleFragment";

    @NonNull
    public static SceneDelegate setupWithActivity(@NonNull final Activity activity, @Nullable Bundle savedInstanceState,
                                                  @NonNull Class<? extends Scene> rootScene,
                                                  boolean supportRestore) {
        return setupWithActivity(activity, savedInstanceState,
                new NavigationSceneOptions().setRootScene(rootScene, null), null, supportRestore);
    }

    @NonNull
    public static SceneDelegate setupWithActivity(@NonNull final Activity activity, @Nullable Bundle savedInstanceState,
                                                  @NonNull Class<? extends Scene> rootScene,
                                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                  boolean supportRestore) {
        return setupWithActivity(activity, savedInstanceState,
                new NavigationSceneOptions().setRootScene(rootScene, null), rootSceneComponentFactory, supportRestore);
    }

    @NonNull
    public static SceneDelegate setupWithActivity(@NonNull final Activity activity, @Nullable Bundle savedInstanceState,
                                                  @NonNull NavigationSceneOptions navigationSceneOptions,
                                                  boolean supportRestore) {
        return setupWithActivity(activity, android.R.id.content, savedInstanceState, navigationSceneOptions, null, supportRestore);
    }

    @NonNull
    public static SceneDelegate setupWithActivity(@NonNull final Activity activity, @Nullable Bundle savedInstanceState,
                                                  @NonNull NavigationSceneOptions navigationSceneOptions,
                                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                  boolean supportRestore) {
        return setupWithActivity(activity, android.R.id.content, savedInstanceState, navigationSceneOptions, rootSceneComponentFactory, supportRestore);
    }

    @NonNull
    public static SceneDelegate setupWithActivity(@NonNull final Activity activity,
                                                  @IdRes int idRes,
                                                  @Nullable Bundle savedInstanceState,
                                                  @NonNull NavigationSceneOptions navigationSceneOptions,
                                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                  final boolean supportRestore) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        LifeCycleFragment lifeCycleFragment = (LifeCycleFragment) fragmentManager.findFragmentByTag(LIFE_CYCLE_FRAGMENT_TAG);

        if (lifeCycleFragment != null && !supportRestore) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(lifeCycleFragment);
            commitFragment(transaction);
            fragmentManager.executePendingTransactions();
            lifeCycleFragment = null;
        }

        if (lifeCycleFragment != null) {
            final ScopeHolderFragment scopeHolderFragment = ScopeHolderFragment.install(activity, false);
            lifeCycleFragment.setRootScopeFactory(new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return scopeHolderFragment.getRootScope();
                }
            });
        } else {
            lifeCycleFragment = LifeCycleFragment.newInstance(supportRestore);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(idRes, lifeCycleFragment, LIFE_CYCLE_FRAGMENT_TAG);
            final NavigationScene navigationScene = (NavigationScene) SceneInstanceUtility.getInstanceFromClass(NavigationScene.class,
                    navigationSceneOptions.toBundle());
            final ScopeHolderFragment scopeHolderFragment = ScopeHolderFragment.install(activity, !supportRestore);
            lifeCycleFragment.setNavigationScene(navigationScene, new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return scopeHolderFragment.getRootScope();
                }
            });
            commitFragment(transaction);
            fragmentManager.executePendingTransactions();
        }
        final LifeCycleFragmentSceneDelegate delegate = new LifeCycleFragmentSceneDelegate(activity, lifeCycleFragment);
        lifeCycleFragment.setNavigationSceneAvailableCallback(new NavigationSceneAvailableCallback() {
            @Override
            public void onNavigationSceneAvailable(NavigationScene navigationScene) {
                delegate.onNavigationSceneAvailable(navigationScene);
            }
        });
        lifeCycleFragment.setRootSceneComponentFactory(rootSceneComponentFactory);
        return delegate;
    }

    private static void commitFragment(FragmentTransaction transaction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            transaction.commitNowAllowingStateLoss();
        } else {
            transaction.commitAllowingStateLoss();
        }
    }
}