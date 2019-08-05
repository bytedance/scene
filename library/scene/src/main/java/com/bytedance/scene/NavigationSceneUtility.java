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
import com.bytedance.scene.utlity.ThreadUtility;

import java.util.HashSet;
import java.util.WeakHashMap;

/**
 * Created by JiangQi on 7/30/18.
 */
public class NavigationSceneUtility {
    private static final String LIFE_CYCLE_FRAGMENT_TAG = "LifeCycleFragment";
    private static final WeakHashMap<Activity, HashSet<String>> CHECK_DUPLICATE_TAG_MAP = new WeakHashMap<>();

    @NonNull
    public static SceneDelegate setupWithActivity(@NonNull final Activity activity, @Nullable Bundle savedInstanceState,
                                                  @NonNull Class<? extends Scene> rootScene,
                                                  boolean supportRestore) {
        return setupWithActivity(activity, savedInstanceState,
                new NavigationSceneOptions(rootScene, null), null, supportRestore);
    }

    @NonNull
    public static SceneDelegate setupWithActivity(@NonNull final Activity activity, @Nullable Bundle savedInstanceState,
                                                  @NonNull Class<? extends Scene> rootScene,
                                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                  boolean supportRestore) {
        return setupWithActivity(activity, savedInstanceState,
                new NavigationSceneOptions(rootScene, null), rootSceneComponentFactory, supportRestore);
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
        return setupWithActivity(activity, idRes, savedInstanceState, navigationSceneOptions,
                rootSceneComponentFactory, supportRestore, LIFE_CYCLE_FRAGMENT_TAG);
    }

    @NonNull
    public static SceneDelegate setupWithActivity(@NonNull final Activity activity,
                                                  @IdRes int idRes,
                                                  @Nullable Bundle savedInstanceState,
                                                  @NonNull NavigationSceneOptions navigationSceneOptions,
                                                  @Nullable SceneComponentFactory rootSceneComponentFactory,
                                                  final boolean supportRestore,
                                                  @NonNull String tag) {
        ThreadUtility.checkUIThread();
        if (tag == null) {
            throw new IllegalArgumentException("tag cant be null");
        }
        checkDuplicateTag(activity, tag);

        FragmentManager fragmentManager = activity.getFragmentManager();
        LifeCycleFragment lifeCycleFragment = (LifeCycleFragment) fragmentManager.findFragmentByTag(tag);
        if (lifeCycleFragment != null && !supportRestore) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(lifeCycleFragment);
            commitFragment(transaction);
            fragmentManager.executePendingTransactions();
            lifeCycleFragment = null;
        }

        if (lifeCycleFragment != null) {
            final ScopeHolderFragment scopeHolderFragment = ScopeHolderFragment.install(activity, tag, false);
            lifeCycleFragment.setRootScopeFactory(new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return scopeHolderFragment.getRootScope();
                }
            });
            lifeCycleFragment.setRootSceneComponentFactory(rootSceneComponentFactory);
        } else {
            lifeCycleFragment = LifeCycleFragment.newInstance(supportRestore);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(idRes, lifeCycleFragment, tag);
            final NavigationScene navigationScene = (NavigationScene) SceneInstanceUtility.getInstanceFromClass(NavigationScene.class,
                    navigationSceneOptions.toBundle());
            final ScopeHolderFragment scopeHolderFragment = ScopeHolderFragment.install(activity, tag, !supportRestore);
            lifeCycleFragment.setNavigationScene(navigationScene, new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return scopeHolderFragment.getRootScope();
                }
            });
            lifeCycleFragment.setRootSceneComponentFactory(rootSceneComponentFactory);
            commitFragment(transaction);
            fragmentManager.executePendingTransactions();
        }
        final LifeCycleFragmentSceneDelegate delegate = new LifeCycleFragmentSceneDelegate(activity, lifeCycleFragment);
        lifeCycleFragment.setNavigationSceneAvailableCallback(new NavigationSceneAvailableCallback() {
            @Override
            public void onNavigationSceneAvailable(@NonNull NavigationScene navigationScene) {
                delegate.onNavigationSceneAvailable(navigationScene);
            }
        });
        return delegate;
    }

    private static void checkDuplicateTag(@NonNull Activity activity, @NonNull String tag) {
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

    private static void commitFragment(FragmentTransaction transaction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            transaction.commitNowAllowingStateLoss();
        } else {
            transaction.commitAllowingStateLoss();
        }
    }
}