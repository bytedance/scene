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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;

/**
 * Created by JiangQi on 9/2/18.
 */
//todo 泛型
public abstract class SceneTransition implements Cloneable {
    @NonNull
    protected View mFromView;
    @NonNull
    protected View mToView;
    @NonNull
    protected View mAnimationView;

    @CallSuper
    public void captureValue(@NonNull View fromView, @NonNull View toView, @NonNull View animationView) {
        if (animationView == null) {
            throw new IllegalArgumentException("animationView cant be null");
        }
        this.mFromView = fromView;
        this.mToView = toView;
        this.mAnimationView = animationView;
    }

    public abstract InteractionAnimation getAnimation(boolean push);

    public Animator getAnimator(final boolean appear) {
        final InteractionAnimation interactionAnimation = getAnimation(appear);
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                interactionAnimation.dispatchProgress((Float) animation.getAnimatedValue());
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                interactionAnimation.dispatchProgress(1.0f);
            }
        });
        return valueAnimator;
    }

    public abstract void finish(boolean push);

    @Override
    public SceneTransition clone() {
        try {
            SceneTransition clone = (SceneTransition) super.clone();
            clone.mFromView = null;
            clone.mToView = null;
            clone.mAnimationView = null;
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
