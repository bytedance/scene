package com.bytedance.scene;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import com.bytedance.scene.utlity.Utility;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 9/12/18.
 */

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
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

    @NonNull
    public static ScopeHolderFragment install(@NonNull Activity activity, @NonNull String tag, boolean forceCreate, boolean immediate) {
        String fragmentTag = tag + "_" + TAG;
        FragmentManager fragmentManager = activity.getFragmentManager();
        ScopeHolderFragment holderFragment = (ScopeHolderFragment) fragmentManager.findFragmentByTag(fragmentTag);
        if (holderFragment != null && forceCreate) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(holderFragment);
            Utility.commitFragment(fragmentManager, transaction, immediate);
            holderFragment = null;
        }

        if (holderFragment == null) {
            holderFragment = ScopeHolderFragment.newInstance();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(holderFragment, fragmentTag);
            Utility.commitFragment(fragmentManager, transaction, immediate);
        }

        return holderFragment;
    }

    public Scope getRootScope() {
        return mRootScope;
    }
}
