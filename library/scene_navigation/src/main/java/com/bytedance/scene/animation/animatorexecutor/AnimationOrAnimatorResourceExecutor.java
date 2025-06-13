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
import android.provider.Settings;
import android.view.View;

import androidx.annotation.AnimRes;
import androidx.annotation.AnimatorRes;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.AnimationOrAnimator;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.utlity.AnimatorUtility;
import com.bytedance.scene.utlity.CancellationSignal;

/**
 * Created by JiangQi on 8/15/18.
 * <p>
 * A -> B
 * A Exit, B Enter
 * <p>
 * B -> A
 * B Return, A Reenter
 */
public final class AnimationOrAnimatorResourceExecutor extends NavigationAnimationExecutor {
    private AnimationOrAnimator mEnterAnimator;
    private AnimationOrAnimator mExitAnimator;
    private AnimationOrAnimator mReturnAnimator;
    private AnimationOrAnimator mReenterAnimator;

    public AnimationOrAnimatorResourceExecutor(Activity activity, @AnimatorRes @AnimRes int enterResId, @AnimatorRes @AnimRes int exitResId) {
        if (enterResId != 0) {
            mEnterAnimator = AnimationOrAnimator.loadAnimation(activity, enterResId);
            mReturnAnimator = AnimationOrAnimator.loadAnimation(activity, enterResId);
            mReturnAnimator.reverse();
        }
        if (exitResId != 0) {
            mExitAnimator = AnimationOrAnimator.loadAnimation(activity, exitResId);
            mReenterAnimator = AnimationOrAnimator.loadAnimation(activity, exitResId);
            mReenterAnimator.reverse();
        }
    }

    public AnimationOrAnimatorResourceExecutor(Activity activity, @AnimatorRes @AnimRes int enterResId, @AnimatorRes @AnimRes int exitResId, @AnimatorRes @AnimRes int returnResId, @AnimatorRes @AnimRes int reenterResId) {
        if (enterResId != 0) {
            mEnterAnimator = AnimationOrAnimator.loadAnimation(activity, enterResId);
        }
        if (exitResId != 0) {
            mExitAnimator = AnimationOrAnimator.loadAnimation(activity, exitResId);
        }
        if (returnResId != 0) {
            mReturnAnimator = AnimationOrAnimator.loadAnimation(activity, returnResId);
        }
        if (reenterResId != 0) {
            mReenterAnimator = AnimationOrAnimator.loadAnimation(activity, reenterResId);
        }
    }

    @Override
    public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
        return true;
    }

    @Override
    public void executePushChangeCancelable(@NonNull final AnimationInfo fromInfo, @NonNull final AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
        if (mEnterAnimator == null && mExitAnimator == null) {
            endAction.run();
            return;
        }
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
        if (mEnterAnimator != null) {
            mEnterAnimator.addEndAction(animationEndAction);
            mEnterAnimator.applySystemDurationScale(toView, Settings.Global.TRANSITION_ANIMATION_SCALE);
            mEnterAnimator.start(toView);
        } else {
            animationEndAction.run();
        }
        if (mExitAnimator != null) {
            mExitAnimator.addEndAction(animationEndAction);
            mExitAnimator.applySystemDurationScale(fromView, Settings.Global.TRANSITION_ANIMATION_SCALE);
            mExitAnimator.start(fromView);
        } else {
            animationEndAction.run();
        }
        cancellationSignal.setOnCancelListener(() -> {
            if (mEnterAnimator != null) {
                mEnterAnimator.end();
            }
            if (mExitAnimator != null) {
                mExitAnimator.end();
            }
        });
    }
    @Override
    public void executePopChangeCancelable(@NonNull AnimationInfo fromInfo, @NonNull AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
        if (mReturnAnimator == null && mReenterAnimator == null) {
            endAction.run();
            return;
        }
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
        if (mReturnAnimator != null) {
            mReturnAnimator.addEndAction(animationEndAction);
            mReturnAnimator.applySystemDurationScale(fromView, Settings.Global.TRANSITION_ANIMATION_SCALE);
            mReturnAnimator.start(fromView);
        } else {
            animationEndAction.run();
        }
        if (mReenterAnimator != null) {
            mReenterAnimator.addEndAction(animationEndAction);
            mReenterAnimator.applySystemDurationScale(toView, Settings.Global.TRANSITION_ANIMATION_SCALE);
            mReenterAnimator.start(toView);
        } else {
            animationEndAction.run();
        }
        cancellationSignal.setOnCancelListener(() -> {
            if (mReturnAnimator != null) {
                mReturnAnimator.end();
            }
            if (mReenterAnimator != null) {
                mReenterAnimator.end();
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
