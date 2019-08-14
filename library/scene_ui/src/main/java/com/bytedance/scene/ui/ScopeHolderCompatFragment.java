package com.bytedance.scene.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
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

    @NonNull
    public static ScopeHolderCompatFragment install(@NonNull Fragment fragment, @NonNull String tag, boolean forceCreate, boolean immediate) {
        String fragmentTag = tag + "_" + TAG;
        FragmentManager fragmentManager = fragment.getChildFragmentManager();
        ScopeHolderCompatFragment holderFragment = (ScopeHolderCompatFragment) fragmentManager.findFragmentByTag(fragmentTag);
        if (holderFragment != null && forceCreate) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(holderFragment);
            FragmentUtility.commitFragment(transaction, immediate);
            holderFragment = null;
        }

        if (holderFragment == null) {
            holderFragment = ScopeHolderCompatFragment.newInstance();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(holderFragment, fragmentTag);
            FragmentUtility.commitFragment(transaction, immediate);
        }

        return holderFragment;
    }

    public Scope getRootScope() {
        return mRootScope;
    }
}