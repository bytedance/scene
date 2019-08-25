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
package com.bytedance.scene.animation.interaction.scenetransition.visiblity;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.animation.interaction.scenetransition.PropertyUtilis;
import com.bytedance.scene.animation.interaction.scenetransition.utils.SceneViewCompatUtils;

/**
 * Created by JiangQi on 10/19/18.
 */
public class Fade extends SceneVisibilityTransition {
    private float mTransitionAlpha;

    @Override
    public void captureValue(@NonNull View view, @NonNull ViewGroup rootView) {
        super.captureValue(view, rootView);
        this.mTransitionAlpha = SceneViewCompatUtils.getTransitionAlpha(view);
    }

    @Override
    public InteractionAnimation getAnimation(boolean appear) {
        if (appear) {
            return new InteractionAnimation(1.0f) {
                @Override
                public void onProgress(float progress) {
                    PropertyUtilis.TRANSITION_ALPHA.set(mView, progress);
                }
            };
        } else {
            return new InteractionAnimation(1.0f) {
                @Override
                public void onProgress(float progress) {
                    PropertyUtilis.TRANSITION_ALPHA.set(mView, 1.0f - progress);
                }
            };
        }
    }

    @Override
    public void onFinish(boolean appear) {
        SceneViewCompatUtils.setTransitionAlpha(mView, 1);
    }
}
