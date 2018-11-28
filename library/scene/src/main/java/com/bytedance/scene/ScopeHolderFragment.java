package com.bytedance.scene;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by JiangQi on 9/12/18.
 */
public class ScopeHolderFragment extends Fragment {
    private static final String TAG = "ScopeHolderFragment";
    private final Scope mRootScope = Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();

    private static ScopeHolderFragment newInstance() {
        return new ScopeHolderFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public static ScopeHolderFragment install(Activity activity, boolean forceCreate) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        ScopeHolderFragment holderFragment = (ScopeHolderFragment) fragmentManager.findFragmentByTag(TAG);
        if (holderFragment != null && forceCreate) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(holderFragment);
            commitFragment(transaction);
            fragmentManager.executePendingTransactions();
            holderFragment = null;
        }

        if (holderFragment == null) {
            holderFragment = ScopeHolderFragment.newInstance();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(holderFragment, TAG);
            commitFragment(transaction);
            fragmentManager.executePendingTransactions();
        }

        return holderFragment;
    }

    private static void commitFragment(FragmentTransaction transaction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            transaction.commitNowAllowingStateLoss();
        } else {
            transaction.commitAllowingStateLoss();
        }
    }

    public Scope getRootScope() {
        return mRootScope;
    }
}
