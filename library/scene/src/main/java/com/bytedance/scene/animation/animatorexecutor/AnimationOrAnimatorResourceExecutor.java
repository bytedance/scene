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

import android.app.Activity;
import android.os.Build;
import android.support.annotation.AnimRes;
import android.support.annotation.AnimatorRes;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.View;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.AnimationOrAnimator;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.utlity.AnimatorUtility;
import com.bytedance.scene.utlity.CancellationSignal;

/**
 * Created by JiangQi on 8/15/18.
 */
public final class AnimationOrAnimatorResourceExecutor extends NavigationAnimationExecutor {
    private AnimationOrAnimator mEnterAnimator;
    private AnimationOrAnimator mExitAnimator;

    public AnimationOrAnimatorResourceExecutor(Activity activity, @AnimatorRes @AnimRes int enterResId, @AnimatorRes @AnimRes int exitResId) {
        mEnterAnimator = AnimationOrAnimator.loadAnimation(activity, enterResId);
        mExitAnimator = AnimationOrAnimator.loadAnimation(activity, exitResId);
    }

    @Override
    public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
        return true;
    }

    @Override
    public void executePushChangeCancelable(@NonNull final AnimationInfo fromInfo, @NonNull final AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
        // Cannot be placed in onAnimationStart, as there it a post interval, it will splash
        final View fromView = fromInfo.mSceneView;
        final View toView = toInfo.mSceneView;

        AnimatorUtility.resetViewStatus(fromView);
        AnimatorUtility.resetViewStatus(toView);
        fromView.setVisibility(View.VISIBLE);

        final float fromViewElevation = ViewCompat.getElevation(fromView);
        if (fromViewElevation > 0) {
            ViewCompat.setElevation(fromView, 0);
        }

        // In the case of pushAndClear, it is possible that the Scene come from has been destroyed.
        if (fromInfo.mSceneState.value < State.VIEW_CREATED.value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mAnimationViewGroup.getOverlay().add(fromView);
            } else {
                mAnimationViewGroup.addView(fromView);
            }
        }

        Runnable animationEndAction = new CountRunnable(2, new Runnable() {
            @Override
            public void run() {
                if (!toInfo.mIsTranslucent) {
                    fromView.setVisibility(View.GONE);
                }

                if (fromViewElevation > 0) {
                    ViewCompat.setElevation(fromView, fromViewElevation);
                }

                AnimatorUtility.resetViewStatus(fromView);
                AnimatorUtility.resetViewStatus(toView);

                if (fromInfo.mSceneState.value < State.VIEW_CREATED.value) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        mAnimationViewGroup.getOverlay().remove(fromView);
                    } else {
                        mAnimationViewGroup.removeView(fromView);
                    }
                }
                endAction.run();
            }
        });

        mEnterAnimator.addEndAction(animationEndAction);
        mExitAnimator.addEndAction(animationEndAction);

        mExitAnimator.start(fromView);
        mEnterAnimator.start(toView);
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                mEnterAnimator.end();
                mExitAnimator.end();
            }
        });
    }

    @Override
    public void executePopChangeCancelable(@NonNull AnimationInfo fromInfo, @NonNull AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
        final View fromView = fromInfo.mSceneView;
        final View toView = toInfo.mSceneView;

        AnimatorUtility.resetViewStatus(fromView);
        AnimatorUtility.resetViewStatus(toView);

        fromView.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mAnimationViewGroup.getOverlay().add(fromView);
        } else {
            mAnimationViewGroup.addView(fromView);
        }

        Runnable animationEndAction = new CountRunnable(2, new Runnable() {
            @Override
            public void run() {
                // Todo: children also has to reset
                AnimatorUtility.resetViewStatus(fromView);
                AnimatorUtility.resetViewStatus(toView);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    mAnimationViewGroup.getOverlay().remove(fromView);
                } else {
                    mAnimationViewGroup.removeView(fromView);
                }
                endAction.run();
            }
        });

        mEnterAnimator.reverse();
        mEnterAnimator.addEndAction(animationEndAction);
        mEnterAnimator.start(fromView);

        mExitAnimator.reverse();
        mExitAnimator.addEndAction(animationEndAction);
        mExitAnimator.start(toView);
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                mEnterAnimator.end();
                mExitAnimator.end();
            }
        });
    }

    private static class CountRunnable implements Runnable {
        int count;
        Runnable runnable;

        private CountRunnable(int count, Runnable runnable) {
            this.count = count;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            count--;
            if (count == 0) {
                runnable.run();
            }
        }
    }
}
