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
import android.util.Property;
import android.view.View;
import android.widget.TextView;

import com.bytedance.scene.animation.interaction.progressanimation.AnimatorFactory;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.animation.interaction.scenetransition.utils.ArgbEvaluator;

public class TextRecolor extends SceneTransition {
    private IntEvaluator mIntEvaluator = new IntEvaluator();
    private int mFromTextColor;
    private int mToTextColor;

    public TextRecolor() {
    }

    @Override
    public void captureValue(View fromView, View toView, View animationView) {
        super.captureValue(fromView, toView, animationView);
        if (this.mFromView instanceof TextView && toView instanceof TextView) {
            this.mFromTextColor = ((TextView) fromView).getCurrentTextColor();
            this.mToTextColor = ((TextView) toView).getCurrentTextColor();
        }
    }

    @Override
    public InteractionAnimation getAnimation(boolean push) {
        if (this.mFromTextColor != this.mToTextColor) {
            ((TextView) (mAnimationView)).setTextColor(this.mFromTextColor);
            AnimatorFactory<TextView, Integer> animatorFactory = new AnimatorFactory<>((TextView) mAnimationView,
                    new Property<TextView, Integer>(Integer.class, "textColor") {
                        @Override
                        public void set(TextView object, Integer value) {
                            object.setTextColor(value);
                        }

                        @Override
                        public Integer get(TextView object) {
                            return object.getCurrentTextColor();
                        }
                    }, ArgbEvaluator.getInstance(), mFromTextColor, mToTextColor, null);
            return animatorFactory.toInteractionAnimation();
        }
        return InteractionAnimation.EMPTY;
    }

    @Override
    public void finish(boolean push) {

    }
}