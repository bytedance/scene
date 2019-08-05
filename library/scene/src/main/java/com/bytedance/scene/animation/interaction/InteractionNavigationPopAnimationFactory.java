package com.bytedance.scene.animation.interaction;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.support.v4.os.CancellationSignal;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.View;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.TransitionUtils;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.navigation.NavigationScene;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 8/9/18.
 */

public abstract class InteractionNavigationPopAnimationFactory {
    public interface InteractionCallback {
        void onStart();

        void onProgress(float progress);

        void onFinish();
    }

    private NavigationScene mNavigationScene;
    private List<InteractionAnimation> mAnimationList;
    private boolean mStart = false;
    private float mProgress;

    private Scene mFromScene;
    private Scene mToScene;

    private Drawable mFromSceneBackground;
    private int mToScreenViewVisibility;

    private InteractionCallback mCallback;

    private CancellationSignal mCancellationSignal;
    private static CancellationSignal sCancellationSignal;

    public static void cancelAllRunningInteractionAnimation() {
        if (sCancellationSignal != null) {
            sCancellationSignal.cancel();
            sCancellationSignal = null;
        }
    }

    public void setCallback(InteractionCallback callback) {
        this.mCallback = callback;
    }

    private class CallbackProgressProxyInteractionAnimation extends InteractionAnimation {
        private CallbackProgressProxyInteractionAnimation(float endProgress) {
            super(endProgress);
        }

        @Override
        public void onProgress(float progress) {
            if (mCallback != null) {
                mCallback.onProgress(progress);
            }
        }
    }

    public void begin(NavigationScene navigationScene, Scene from, Scene to) {
        this.mStart = true;
        this.mNavigationScene = navigationScene;
        this.mFromScene = from;
        this.mToScene = to;
        this.mAnimationList = onPopInteraction(from, to);

        if (mCallback != null) {
            this.mAnimationList.add(new CallbackProgressProxyInteractionAnimation(1.0f));
        }

        this.mToScreenViewVisibility = this.mToScene.getView().getVisibility();
        this.mToScene.getView().setVisibility(View.VISIBLE);
        this.mFromSceneBackground = this.mFromScene.getView().getBackground();
        ViewCompat.setBackground(this.mFromScene.getView(), null);

        mCancellationSignal = new CancellationSignal();
        mCancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                forceCancel();
            }
        });
        sCancellationSignal = mCancellationSignal;

        if (this.mCallback != null) {
            this.mCallback.onStart();
        }
    }

    public void finish() {
        if (!this.mStart) {
            return;
        }
        this.mStart = false;

        float progress = this.mProgress;
        this.mProgress = 0;

        float dstProgress = 0;
        final boolean canExit = canExit(progress);
        if (canExit) {
            dstProgress = 1.0f;
        } else {
            dstProgress = 0.0f;
        }

        List<Animator> animatorList = new ArrayList<>();
        for (InteractionAnimation animation : mAnimationList) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(animation, InteractionAnimation.INTERACTION_ANIMATION_FLOAT_PROPERTY, animation.getCurrentProgress(), dstProgress);
            objectAnimator.setDuration(150);
            animatorList.add(objectAnimator);
        }

        final Animator animator = TransitionUtils.mergeAnimators(animatorList);
        animator.addListener(new AnimatorListenerAdapter() {
            boolean isCanceled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                isCanceled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (canExit && !isCanceled) {
                    popWithoutAnimation();
                } else {
                    restoreStatusNow();
                }
            }
        });
        animator.setInterpolator(new LinearOutSlowInInterpolator());
        animator.start();
        mAnimationList = null;

        mCancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                animator.cancel();
            }
        });
    }

    private void resetCancellationSignal() {
        if (sCancellationSignal == mCancellationSignal) {
            sCancellationSignal = null;
        }
    }

    /**
     * Can be closed, normal animation ends with exit
     */
    private void popWithoutAnimation() {
        resetCancellationSignal();
        mNavigationScene.convertBackgroundToDefault();
        onInteractionEnd();
        if (this.mCallback != null) {
            this.mCallback.onFinish();
        }
    }

    /**
     * Forced to cancel midway
     */
    public void forceCancel() {
        if (!this.mStart) {
            return;
        }
        updateProgress(0.0f);
        this.mStart = false;
        restoreStatusNow();
    }

    /**
     * Cannot close, normal animation ends reset state
     */
    private void restoreStatusNow() {
        resetCancellationSignal();
        mNavigationScene.convertBackgroundToDefault();
        mToScene.getView().setVisibility(mToScreenViewVisibility);
        ViewCompat.setBackground(mFromScene.getView(), mFromSceneBackground);
        onInteractionCancel();

        if (this.mCallback != null) {
            this.mCallback.onFinish();
        }
    }

    protected abstract boolean canExit(float progress);

    protected abstract void onInteractionCancel();

    protected abstract void onInteractionEnd();

    public abstract boolean isSupport(Scene from, Scene to);

    protected abstract List<InteractionAnimation> onPopInteraction(Scene from, Scene to);

    public void updateProgress(float progress) {
        if (!this.mStart) {
            return;
        }

        for (InteractionAnimation animation : mAnimationList) {
            animation.dispatchProgress(progress);
        }
        this.mProgress = progress;
    }
}
