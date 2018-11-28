package com.bytedance.scene.animation.interaction.progressanimation;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Property;

/**
 * Created by JiangQi on 10/23/18.
 */
public class AnimatorFactory<T, F> {
    @NonNull
    private final T mTarget;
    @NonNull
    private final Property<T, F> mProperty;

    @NonNull
    private final TypeEvaluator<F> mTypeEvaluator;
    @NonNull
    private final F mStartValue;
    @NonNull
    private final F mEndValue;

    @Nullable
    private TimeInterpolator mInterpolator;

    public AnimatorFactory(@NonNull T target, @NonNull Property<T, F> property,
                           @NonNull TypeEvaluator<F> typeEvaluator, @NonNull F startValue, @NonNull F endValue,
                           @Nullable TimeInterpolator interpolator) {
        this.mTarget = target;
        this.mTypeEvaluator = typeEvaluator;
        this.mProperty = property;
        this.mStartValue = startValue;
        this.mEndValue = endValue;
        this.mInterpolator = interpolator;
    }

    private void progress(float fraction) {
        if (mInterpolator != null) {
            fraction = mInterpolator.getInterpolation(fraction);
        }
        mProperty.set(mTarget, mTypeEvaluator.evaluate(fraction, mStartValue, mEndValue));
    }

    public InteractionAnimation toInteractionAnimation() {
        return new InteractionAnimation() {
            @Override
            public void onProgress(float progress) {
                progress(progress);
            }
        };
    }

    public Animator toAnimator() {
        Animator animator = ObjectAnimator.ofObject(mTarget, mProperty, mTypeEvaluator, mStartValue, mEndValue);
        if (this.mInterpolator != null) {
            animator.setInterpolator(this.mInterpolator);
        }
        return animator;
    }
}
