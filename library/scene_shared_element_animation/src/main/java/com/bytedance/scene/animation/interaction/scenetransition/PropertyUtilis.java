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
package com.bytedance.scene.animation.interaction.scenetransition;

import android.graphics.Rect;
import androidx.core.view.ViewCompat;
import android.util.Property;
import android.view.View;

import com.bytedance.scene.animation.interaction.scenetransition.utils.SceneViewCompatUtils;

/**
 * Created by JiangQi on 10/19/18.
 */
public class PropertyUtilis {
    public static final Property<View, Rect> CLIP_BOUNDS =
            new Property<View, Rect>(Rect.class, "clipBounds") {

                @Override
                public Rect get(View view) {
                    return ViewCompat.getClipBounds(view);
                }

                @Override
                public void set(View view, Rect clipBounds) {
                    ViewCompat.setClipBounds(view, clipBounds);
                }

            };

    public static final Property<View, Float> TRANSITION_ALPHA =
            new Property<View, Float>(Float.class, "translationAlpha") {

                @Override
                public Float get(View view) {
                    return SceneViewCompatUtils.getTransitionAlpha(view);
                }

                @Override
                public void set(View view, Float alpha) {
                    SceneViewCompatUtils.setTransitionAlpha(view, alpha);
                }

            };
}
