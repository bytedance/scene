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
package com.bytedance.scene.utlity;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;

import com.bytedance.scene.navigation.R;

/**
 * Created by JiangQi on 8/24/18.
 * <p>
 * consumeStableInsets will be consumed by DecorView#updateColorViews
 */

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DispatchWindowInsetsListener implements View.OnApplyWindowInsetsListener {
    private final boolean mOnlyDispatchToTopScene;

    public DispatchWindowInsetsListener() {
        this(false);
    }

    public DispatchWindowInsetsListener(boolean onlyDispatchToTopScene) {
        this.mOnlyDispatchToTopScene = onlyDispatchToTopScene;
    }

    @Override
    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
        WindowInsets copy = new WindowInsets(insets);
        ViewGroup viewGroup = (ViewGroup) v;
        final int count = viewGroup.getChildCount();
        if (!this.mOnlyDispatchToTopScene) {
            for (int i = 0; i < count; i++) {
                viewGroup.getChildAt(i).dispatchApplyWindowInsets(copy);
            }
        } else {
            for (int i = count - 1; i >= 0; i--) {
                View childView = viewGroup.getChildAt(i);
                //if view is the animation view, dispatch directly
                Boolean isAnimationView = (Boolean) childView.getTag(R.id.navigation_scene_animation_view);
                if (isAnimationView != null && isAnimationView) {
                    childView.dispatchApplyWindowInsets(copy);
                } else {
                    if (childView.getTag(R.id.bytedance_scene_view_scene_tag) != null) {
                        childView.dispatchApplyWindowInsets(copy);
                        break;
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return WindowInsets.CONSUMED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return insets.consumeSystemWindowInsets().consumeDisplayCutout();
        } else {
            return insets.consumeSystemWindowInsets();
        }
    }

    public static void markAnimationView(View animationContainerLayout) {
        animationContainerLayout.setTag(R.id.navigation_scene_animation_view, true);
    }
}
