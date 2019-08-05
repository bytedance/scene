package com.bytedance.scene;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.support.annotation.NonNull;

import android.support.annotation.Nullable;
import com.bytedance.scene.navigation.NavigationScene;

/**
 * Created by JiangQi on 9/11/18.
 */
final class LifeCycleFragmentSceneDelegate implements SceneDelegate, NavigationSceneAvailableCallback {
    private final Activity mActivity;
    private final LifeCycleFragment mLifeCycleFragment;
    private final ScopeHolderFragment mScopeHolderFragment;

    private NavigationScene mNavigationScene;
    private NavigationSceneAvailableCallback mNavigationSceneAvailableCallback;
    private boolean mAbandoned = false;

    LifeCycleFragmentSceneDelegate(@NonNull Activity activity, @NonNull LifeCycleFragment lifeCycleFragment, @NonNull ScopeHolderFragment scopeHolderFragment) {
        this.mActivity = activity;
        this.mLifeCycleFragment = lifeCycleFragment;
        this.mScopeHolderFragment = scopeHolderFragment;
    }

    @Override
    public boolean onBackPressed() {
        NavigationScene navigationScene = mLifeCycleFragment.getNavigationScene();
        return !this.mAbandoned && navigationScene != null && navigationScene.onBackPressed();
    }

    @Override
    @Nullable
    public NavigationScene getNavigationScene() {
        if (this.mAbandoned) {
            return null;
        }
        return mLifeCycleFragment.getNavigationScene();
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
        this.mLifeCycleFragment.setLifecycleFragmentDetachCallback(new LifeCycleFragment.LifecycleFragmentDetachCallback() {
            @Override
            public void onDetach() {
                NavigationSceneUtility.removeTag(mActivity, mLifeCycleFragment.getTag());
            }
        });
        FragmentManager fragmentManager = mActivity.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().remove(this.mLifeCycleFragment).remove(this.mScopeHolderFragment);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fragmentTransaction.commitNowAllowingStateLoss();
        } else {
            fragmentTransaction.commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
    }
}
