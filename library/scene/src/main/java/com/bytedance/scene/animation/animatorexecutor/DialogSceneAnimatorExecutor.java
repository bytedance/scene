package com.bytedance.scene.animation.animatorexecutor;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimatorExecutor;

/**
 * Created by JiangQi on 8/9/18.
 */
public class DialogSceneAnimatorExecutor extends NavigationAnimatorExecutor {
    @Override
    public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
        return true;
    }

    @Override
    protected boolean disableConfigAnimationDuration() {
        return true;
    }

    //todo 不要在动画过程中操作to.getView，万一已经变了怎么办？
    @NonNull
    @Override
    protected Animator onPushAnimator(AnimationInfo from, final AnimationInfo to) {
        final View toView = to.mSceneView;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                toView.setAlpha((float) animation.getAnimatedValue());
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(150);
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
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(150);
        return valueAnimator;
    }
}
