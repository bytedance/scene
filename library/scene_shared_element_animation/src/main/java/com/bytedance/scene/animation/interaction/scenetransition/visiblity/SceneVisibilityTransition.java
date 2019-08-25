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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.animation.interaction.scenetransition.visiblity.transitionpropagation.TransitionPropagation;
import com.bytedance.scene.animation.interaction.scenetransition.visiblity.transitionpropagation.TransitionPropagationResult;

public abstract class SceneVisibilityTransition implements Cloneable {
    @NonNull
    protected View mView;
    @NonNull
    protected ViewGroup mRootView;
    @Nullable
    private SceneVisibilityTransition.EpicenterCallback mEpicenterCallback;
    @Nullable
    private TransitionPropagation mPropagation;
    @Nullable
    private TransitionPropagationResult mTransitionPropagationResult;

    private long mDuration = 300L;

    @CallSuper
    public void captureValue(@NonNull View view, @NonNull ViewGroup rootView) {
        this.mView = view;
        this.mRootView = rootView;
        if (this.mPropagation != null) {
            this.mTransitionPropagationResult = mPropagation.captureValues(view, rootView);
        }
    }

    public abstract InteractionAnimation getAnimation(boolean appear);

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
                onFinish(appear);
            }
        });
        if (this.mPropagation != null) {
            valueAnimator.setStartDelay(this.mPropagation.getStartDelay(this.mRootView, this, this.mTransitionPropagationResult, appear));
        }
        valueAnimator.setDuration(this.mDuration);
        return valueAnimator;
    }

    public abstract void onFinish(boolean appear);

    @Override
    public SceneVisibilityTransition clone() {
        try {
            SceneVisibilityTransition clone = (SceneVisibilityTransition) super.clone();
            clone.mView = null;
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Nullable
    public Rect getEpicenter() {
        if (mEpicenterCallback == null) {
            return null;
        }
        return mEpicenterCallback.onGetEpicenter(this);
    }

    public void setPropagation(@Nullable TransitionPropagation transitionPropagation) {
        mPropagation = transitionPropagation;
    }

    @Nullable
    public TransitionPropagation getPropagation() {
        return mPropagation;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        this.mDuration = duration;
    }

    /**
     * Used to calculate the animation's startDelay
     */
    public abstract static class EpicenterCallback {
        public abstract Rect onGetEpicenter(@NonNull SceneVisibilityTransition transition);
    }
}
