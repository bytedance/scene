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