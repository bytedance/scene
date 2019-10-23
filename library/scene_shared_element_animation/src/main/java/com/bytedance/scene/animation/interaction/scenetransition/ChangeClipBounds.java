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
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import android.view.View;

import com.bytedance.scene.animation.interaction.evaluator.RectEvaluator;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;

/**
 * Created by JiangQi on 10/19/18.
 */
public class ChangeClipBounds extends SceneTransition {
    private static Rect captureValues(View view) {
        if (view.getVisibility() == View.GONE) {
            return null;
        }

        return ViewCompat.getClipBounds(view);
    }

    private Rect mFromRect;
    private Rect mToRect;
    private static final RectEvaluator RECT_EVALUATOR = new RectEvaluator(new Rect());

    @Override
    public void captureValue(@NonNull View fromView, @NonNull View toView, @NonNull View animationView) {
        super.captureValue(fromView, toView, animationView);
        this.mFromRect = captureValues(fromView);
        this.mToRect = captureValues(toView);
    }

    @Override
    public InteractionAnimation getAnimation(boolean push) {
        if (this.mFromRect == null || this.mToRect == null || this.mFromRect.equals(this.mToRect)) {
            return InteractionAnimation.EMPTY;
        }
        return new InteractionAnimation() {
            @Override
            public void onProgress(float progress) {
                PropertyUtilis.CLIP_BOUNDS.set(mAnimationView, RECT_EVALUATOR.evaluate(progress, mFromRect, mToRect));
            }
        };
    }

    @Override
    public void finish(boolean push) {
        ViewCompat.setClipBounds(mAnimationView, null);
    }
}
