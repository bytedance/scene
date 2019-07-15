package com.bytedance.scene.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.View;

import com.bytedance.scene.utlity.AnimatorUtility;
import com.bytedance.scene.utlity.CancellationSignal;
import com.bytedance.scene.utlity.EnableLayerAnimationListener;

/**
 * Created by JiangQi on 8/15/18.
 */
public abstract class NavigationAnimatorExecutor extends NavigationAnimationExecutor {
    private static final int DURATION = 300;

    protected boolean disableConfigAnimationDuration() {
        return false;
    }

    protected boolean enableViewLayer() {
        return true;
    }

    @Override
    public final void executePushChangeCancelable(@NonNull final AnimationInfo fromInfo, @NonNull final AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
        //不能放onAnimationStart，因为会有post间隔，会闪屏
        final View fromView = fromInfo.mSceneView;
        final View toView = toInfo.mSceneView;

        AnimatorUtility.AnimatorInfo fromViewAnimatorInfo = null;
        if (fromInfo.mIsTranslucent) {
            fromViewAnimatorInfo = AnimatorUtility.captureViewStatus(fromView);
        } else {
            AnimatorUtility.resetViewStatus(fromView);
        }

        AnimatorUtility.AnimatorInfo toViewAnimatorInfo = null;
        if (toInfo.mIsTranslucent) {
            toViewAnimatorInfo = AnimatorUtility.captureViewStatus(toView);
        } else {
            AnimatorUtility.resetViewStatus(toView);
        }

        fromView.setVisibility(View.VISIBLE);

        final float fromViewElevation = ViewCompat.getElevation(fromView);
        if (fromViewElevation > 0) {
            ViewCompat.setElevation(fromView, 0);
        }

        final Animator animator = onPushAnimator(fromInfo, toInfo);
        if (!disableConfigAnimationDuration()) {
            animator.setDuration(DURATION);
        }
        final AnimatorUtility.AnimatorInfo finalFromViewAnimatorInfo = fromViewAnimatorInfo;
        final AnimatorUtility.AnimatorInfo finalToViewAnimatorInfo = toViewAnimatorInfo;
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!toInfo.mIsTranslucent) {
                    fromView.setVisibility(View.GONE);
                }

                if (fromViewElevation > 0) {
                    ViewCompat.setElevation(fromView, fromViewElevation);
                }

                if (fromInfo.mIsTranslucent) {
                    AnimatorUtility.resetViewStatus(fromView, finalFromViewAnimatorInfo);
                } else {
                    AnimatorUtility.resetViewStatus(fromView);
                }
                if (toInfo.mIsTranslucent) {
                    AnimatorUtility.resetViewStatus(toView, finalToViewAnimatorInfo);
                } else {
                    AnimatorUtility.resetViewStatus(toView);
                }

                endAction.run();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
        if (enableViewLayer()) {
            animator.addListener(new EnableLayerAnimationListener(fromView));
            animator.addListener(new EnableLayerAnimationListener(toView));
        }
        animator.start();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                animator.end();
            }
        });
    }

    @Override
    public final void executePopChangeCancelable(@NonNull final AnimationInfo fromInfo, @NonNull final AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
        final View fromView = fromInfo.mSceneView;
        final View toView = toInfo.mSceneView;

        AnimatorUtility.AnimatorInfo fromViewAnimatorInfo = null;
        if (fromInfo.mIsTranslucent) {
            fromViewAnimatorInfo = AnimatorUtility.captureViewStatus(fromView);
        } else {
            AnimatorUtility.resetViewStatus(fromView);
        }

        AnimatorUtility.AnimatorInfo toViewAnimatorInfo = null;
        if (toInfo.mIsTranslucent) {
            toViewAnimatorInfo = AnimatorUtility.captureViewStatus(toView);
        } else {
            AnimatorUtility.resetViewStatus(toView);
        }

        fromView.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mAnimationViewGroup.getOverlay().add(fromView);
        } else {
            mAnimationViewGroup.addView(fromView);
        }

        final Animator animator = onPopAnimator(fromInfo, toInfo);
        if (!disableConfigAnimationDuration()) {
            animator.setDuration(DURATION);
        }
        final AnimatorUtility.AnimatorInfo finalFromViewAnimatorInfo = fromViewAnimatorInfo;
        final AnimatorUtility.AnimatorInfo finalToViewAnimatorInfo = toViewAnimatorInfo;
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //todo child是不是也得reset
                if (fromInfo.mIsTranslucent) {
                    AnimatorUtility.resetViewStatus(fromView, finalFromViewAnimatorInfo);
                } else {
                    AnimatorUtility.resetViewStatus(fromView);
                }
                if (toInfo.mIsTranslucent) {
                    AnimatorUtility.resetViewStatus(toView, finalToViewAnimatorInfo);
                } else {
                    AnimatorUtility.resetViewStatus(toView);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    mAnimationViewGroup.getOverlay().remove(fromView);
                } else {
                    mAnimationViewGroup.removeView(fromView);
                }
                endAction.run();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
        if (enableViewLayer()) {
            animator.addListener(new EnableLayerAnimationListener(fromView));
            animator.addListener(new EnableLayerAnimationListener(toView));
        }

        animator.start();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                animator.end();
            }
        });
    }

    @NonNull
    protected abstract Animator onPushAnimator(AnimationInfo fromInfo, AnimationInfo toInfo);

    @NonNull
    protected abstract Animator onPopAnimator(AnimationInfo fromInfo, AnimationInfo toInfo);
}
