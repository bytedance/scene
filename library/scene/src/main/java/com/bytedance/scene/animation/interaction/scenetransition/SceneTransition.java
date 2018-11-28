package com.bytedance.scene.animation.interaction.scenetransition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.view.View;

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
