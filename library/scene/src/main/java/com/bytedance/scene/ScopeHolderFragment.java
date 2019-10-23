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
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import com.bytedance.scene.utlity.Utility;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 9/12/18.
 */

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ScopeHolderFragment extends Fragment implements Scope.RootScopeFactory {
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

    @NonNull
    @Override
    public Scope getRootScope() {
        return mRootScope;
    }
}
