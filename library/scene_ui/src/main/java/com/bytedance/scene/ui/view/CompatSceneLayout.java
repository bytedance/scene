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
package com.bytedance.scene.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import com.bytedance.scene.ui.R;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class CompatSceneLayout extends LinearLayout {
    public CompatSceneLayout(@NonNull Context context) {
        super(context);
    }

    public CompatSceneLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CompatSceneLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CompatSceneLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        StatusBarView statusBarView = findViewById(R.id.scene_status_bar);
        if (statusBarView != null) {
            if (statusBarView.getParent() != this) {
                throw new IllegalStateException("StatusBarView parent must be " + getClass().getSimpleName());
            }
            insets = statusBarView.dispatchApplyWindowInsets(insets);
        }

        if (insets.isConsumed()) {
            return insets;
        }

        NavigationBarView navigationBarView = findViewById(R.id.scene_navigation_bar);
        if (navigationBarView != null) {
            if (navigationBarView.getParent() != this) {
                throw new IllegalStateException("NavigationBarView parent must be " + getClass().getSimpleName());
            }
            insets = navigationBarView.dispatchApplyWindowInsets(insets);
        }

        if (insets.isConsumed()) {
            return insets;
        }

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View childView = getChildAt(i);
            if (childView == statusBarView || childView == navigationBarView) {
                continue;
            }
            insets = childView.dispatchApplyWindowInsets(insets);
            if (insets.isConsumed()) {
                break;
            }
        }
        return insets;
    }
}
