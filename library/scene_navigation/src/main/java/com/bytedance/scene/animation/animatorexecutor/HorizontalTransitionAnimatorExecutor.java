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
import android.animation.ValueAnimator;
import androidx.annotation.NonNull;
import android.view.View;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimatorExecutor;
import com.bytedance.scene.animation.TransitionUtils;

/**
 * Created by JiangQi on 8/1/18.
 */
public class HorizontalTransitionAnimatorExecutor extends NavigationAnimatorExecutor {
    @Override
    public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
        return true;
    }

    @NonNull
    @Override
    protected Animator onPushAnimator(final AnimationInfo from, final AnimationInfo to) {
        ValueAnimator fromViewAnimator = ValueAnimator.ofInt(0, -from.mSceneView.getWidth());
        fromViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                from.mSceneView.setTranslationX((int) animation.getAnimatedValue());
            }
        });

        ValueAnimator toViewAnimator = ValueAnimator.ofInt(to.mSceneView.getWidth(), 0);
        toViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                to.mSceneView.setTranslationX((int) animation.getAnimatedValue());
            }
        });
        return TransitionUtils.mergeAnimators(fromViewAnimator, toViewAnimator);
    }

    @NonNull
    @Override
    protected Animator onPopAnimator(final AnimationInfo fromInfo, final AnimationInfo toInfo) {
        final View fromView = fromInfo.mSceneView;
        final View toView = toInfo.mSceneView;
        ValueAnimator fromViewAnimator = ValueAnimator.ofInt(0, fromView.getWidth());
        fromViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                fromView.setTranslationX((int) animation.getAnimatedValue());
            }
        });

        ValueAnimator toViewAnimator = ValueAnimator.ofInt(-toView.getWidth(), 0);
        toViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                toView.setTranslationX((int) animation.getAnimatedValue());
            }
        });
        return TransitionUtils.mergeAnimators(fromViewAnimator, toViewAnimator);
    }
}
