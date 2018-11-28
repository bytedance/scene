package com.bytedance.scene.animation.animatorexecutor;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimatorExecutor;

/**
 * Created by JiangQi on 8/1/18.
 */
public class AlphaNavigationSceneAnimatorExecutor extends NavigationAnimatorExecutor {
    @Override
    public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
        return true;
    }

    @NonNull
    @Override
    protected Animator onPushAnimator(AnimationInfo from, final AnimationInfo to) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                to.mSceneView.setAlpha((float) animation.getAnimatedValue());
            }
        });
        return valueAnimator;
    }

    @NonNull
    @Override
    protected Animator onPopAnimator(final AnimationInfo fromInfo, final AnimationInfo toInfo) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                fromInfo.mSceneView.setAlpha((float) animation.getAnimatedValue());
            }
        });
        return valueAnimator;
    }
}
