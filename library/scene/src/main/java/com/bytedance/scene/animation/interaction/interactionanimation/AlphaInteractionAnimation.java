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
package com.bytedance.scene.animation.interaction.interactionanimation;

import android.animation.FloatEvaluator;
import android.util.Property;
import android.view.View;

import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;

/**
 * Created by JiangQi on 8/9/18.
 */
public class AlphaInteractionAnimation extends InteractionAnimation {
    private FloatEvaluator evaluator = new FloatEvaluator();
    private Property<View, Float> property = View.ALPHA;
    private View mTargetView;
    private float mFromValue;
    private float mToValue;

    public AlphaInteractionAnimation(View targetView, float fromValue, float toValue, float endProgress) {
        super(endProgress);
        this.mTargetView = targetView;
        this.mFromValue = fromValue;
        this.mToValue = toValue;
    }

    @Override
    public void onProgress(float progress) {
        property.set(mTargetView, evaluator.evaluate(progress, this.mFromValue, this.mToValue));
    }
}
