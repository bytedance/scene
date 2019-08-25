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
package com.bytedance.scene.animation.animatorexecutor;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimatorExecutor;
import com.bytedance.scene.animation.TransitionUtils;

/**
 * Created by JiangQi on 8/3/18.
 */
public class Android8DefaultSceneAnimatorExecutor extends NavigationAnimatorExecutor {
    private DialogSceneAnimatorExecutor mDialogSceneAnimatorExecutor = new DialogSceneAnimatorExecutor();

    @Override
    public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
        return true;
    }

    @Override
    protected boolean disableConfigAnimationDuration() {
        return true;
    }

    @NonNull
    @Override
    protected Animator onPushAnimator(AnimationInfo from, final AnimationInfo to) {
        if (to.mIsTranslucent) {
            return mDialogSceneAnimatorExecutor.onPushAnimator(from, to);
        }
        final View fromView = from.mSceneView;
        final View toView = to.mSceneView;

        ValueAnimator fromAlphaAnimator = ObjectAnimator.ofFloat(fromView, View.ALPHA, 1.0f, 1.0f);//之前是0.7，但是动画后面会露出NavigationScene的背景色白色很怪异
        fromAlphaAnimator.setInterpolator(new FastOutSlowInInterpolator());
        fromAlphaAnimator.setDuration(120);

        ValueAnimator toAlphaAnimator = ObjectAnimator.ofFloat(toView, View.ALPHA, 0.0f, 1.0f);
        toAlphaAnimator.setInterpolator(new DecelerateInterpolator(2));
        toAlphaAnimator.setDuration(120);

        ValueAnimator toTranslateAnimator = ObjectAnimator.ofFloat(toView, View.TRANSLATION_Y, 0.08f * toView.getHeight(), 0);
        toTranslateAnimator.setInterpolator(new DecelerateInterpolator(2.5f));
        toTranslateAnimator.setDuration(200);
        return TransitionUtils.mergeAnimators(fromAlphaAnimator, toAlphaAnimator, toTranslateAnimator);
    }

    @NonNull
    @Override
    protected Animator onPopAnimator(final AnimationInfo fromInfo, final AnimationInfo toInfo) {
        if (fromInfo.mIsTranslucent) {
            return mDialogSceneAnimatorExecutor.onPopAnimator(fromInfo, toInfo);
        }
        final View toView = toInfo.mSceneView;
        final View fromView = fromInfo.mSceneView;

        ValueAnimator fromAlphaAnimator = ObjectAnimator.ofFloat(fromView, View.ALPHA, 1.0f, 0.0f);
        fromAlphaAnimator.setInterpolator(new LinearInterpolator());
        fromAlphaAnimator.setDuration(150);
        fromAlphaAnimator.setStartDelay(50);

        ValueAnimator fromTranslateAnimator = ObjectAnimator.ofFloat(fromView, View.TRANSLATION_Y, 0, 0.08f * toView.getHeight());
        fromTranslateAnimator.setInterpolator(new AccelerateInterpolator(2));
        fromTranslateAnimator.setDuration(200);

        ValueAnimator toAlphaAnimator = ObjectAnimator.ofFloat(toView, View.ALPHA, 0.7f, 1.0f);
        toAlphaAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        toAlphaAnimator.setDuration(200);
        return TransitionUtils.mergeAnimators(fromAlphaAnimator, fromTranslateAnimator, toAlphaAnimator);
    }
}
