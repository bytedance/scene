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

import android.animation.IntEvaluator;
import android.support.annotation.NonNull;
import android.view.View;

import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;

/**
 * Created by JiangQi on 10/19/18.
 */
public class ChangeScroll extends SceneTransition {
    private int mFromScrollX;
    private int mFromScrollY;

    private int mToScrollX;
    private int mToScrollY;

    private final IntEvaluator mEvaluator = new IntEvaluator();

    @Override
    public void captureValue(@NonNull View fromView, @NonNull View toView, @NonNull View animationView) {
        super.captureValue(fromView, toView, animationView);
        this.mFromScrollX = fromView.getScrollX();
        this.mFromScrollY = fromView.getScrollY();
        this.mToScrollX = toView.getScrollX();
        this.mToScrollY = toView.getScrollY();
    }

    @Override
    public InteractionAnimation getAnimation(boolean push) {
        return new InteractionAnimation(1.0f) {
            @Override
            public void onProgress(float progress) {
                mAnimationView.setScrollX(mEvaluator.evaluate(progress, mFromScrollX, mToScrollX));
                mAnimationView.setScrollY(mEvaluator.evaluate(progress, mFromScrollY, mToScrollY));
            }
        };
    }

    @Override
    public void finish(boolean push) {

    }

//    scrollXAnimator = ObjectAnimator.ofInt(view, "scrollX", startX, endX);
}
