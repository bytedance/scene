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

import android.content.res.Resources;
import android.view.View;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.fragment.app.Fragment;
import com.bytedance.scene.ViewFinder;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;


/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class FragmentViewFinder implements ViewFinder {
    private final Fragment mFragment;

    FragmentViewFinder(@NonNull Fragment fragment) {
        this.mFragment = fragment;
    }

    @NonNull
    @Override
    public <T extends View> T requireViewById(@IdRes int viewId) {
        T view = mFragment.getView().findViewById(viewId);
        if (view == null) {
            try {
                String viewIdName = mFragment.getResources().getResourceName(viewId);
                throw new IllegalArgumentException(" " + viewIdName + " view not found");
            } catch (Resources.NotFoundException exception) {
                throw new IllegalArgumentException(" " + viewId + " view not found");
            }
        }
        return view;
    }
}
