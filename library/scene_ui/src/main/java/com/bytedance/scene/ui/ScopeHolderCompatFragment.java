package com.bytedance.scene.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.bytedance.scene.Scope;

public class ScopeHolderCompatFragment extends Fragment {
    private static final String TAG = "ScopeHolderCompatFragment";
    private final Scope mRootScope = Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();

    private static ScopeHolderCompatFragment newInstance() {
        return new ScopeHolderCompatFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public static ScopeHolderCompatFragment install(Fragment fragment, boolean forceCreate) {
        FragmentManager fragmentManager = fragment.getChildFragmentManager();
        ScopeHolderCompatFragment holderFragment = (ScopeHolderCompatFragment) fragmentManager.findFragmentByTag(TAG);
        if (holderFragment != null && forceCreate) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(holderFragment);
            commitFragment(transaction);
            fragmentManager.executePendingTransactions();
            holderFragment = null;
        }

        if (holderFragment == null) {
            holderFragment = ScopeHolderCompatFragment.newInstance();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(holderFragment, TAG);
            commitFragment(transaction);
            fragmentManager.executePendingTransactions();
        }

        return holderFragment;
    }

    private static void commitFragment(FragmentTransaction transaction) {
        transaction.commitNowAllowingStateLoss();
    }

    public Scope getRootScope() {
        return mRootScope;
    }
}