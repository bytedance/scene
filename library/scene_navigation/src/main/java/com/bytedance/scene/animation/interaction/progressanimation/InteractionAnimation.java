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
package com.bytedance.scene.animation.interaction.progressanimation;

import android.util.Property;

/**
 * Created by JiangQi on 8/9/18.
 */
public abstract class InteractionAnimation {
    public static InteractionAnimation EMPTY = new InteractionAnimation(1.0f) {
        @Override
        public void onProgress(float progress) {

        }
    };
    public static final Property<InteractionAnimation, Float> INTERACTION_ANIMATION_FLOAT_PROPERTY = new Property<InteractionAnimation, Float>(Float.class, "") {
        @Override
        public Float get(InteractionAnimation object) {
            return null;
        }

        @Override
        public void set(InteractionAnimation object, Float value) {
            object.onProgress(value);
        }
    };

    private float mEndProgress;
    private float mCurrentProgress;

    public InteractionAnimation() {
        this(1.0f);
    }

    public InteractionAnimation(float endProgress) {
        this.mEndProgress = endProgress;
    }

    public void dispatchProgress(float progress) {
        mCurrentProgress = Math.min(1.0f, progress / this.mEndProgress);
        onProgress(mCurrentProgress);
    }

    public float getCurrentProgress() {
        return this.mCurrentProgress;
    }

    public abstract void onProgress(float progress);
}
