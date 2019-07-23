package com.bytedance.scene.animation.interaction.scenetransition;

import android.animation.IntEvaluator;
import android.support.annotation.NonNull;
import android.view.View;

import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;

/**
 * Created by JiangQi on 10/19/18.
 */
public class ChangeScroll extends SceneTransition {
    private int mFromScrollX;
    private int mFromScrollY;

    private int mToScrollX;
    private int mToScrollY;

    private final IntEvaluator mEvaluator = new IntEvaluator();

    @Override
    public void captureValue(@NonNull View fromView, @NonNull View toView, @NonNull View animationView) {
        super.captureValue(fromView, toView, animationView);
        this.mFromScrollX = fromView.getScrollX();
        this.mFromScrollY = fromView.getScrollY();
        this.mToScrollX = toView.getScrollX();
        this.mToScrollY = toView.getScrollY();
    }

    @Override
    public InteractionAnimation getAnimation(boolean push) {
        return new InteractionAnimation(1.0f) {
            @Override
            public void onProgress(float progress) {
                mAnimationView.setScrollX(mEvaluator.evaluate(progress, mFromScrollX, mToScrollX));
                mAnimationView.setScrollY(mEvaluator.evaluate(progress, mFromScrollY, mToScrollY));
            }
        };
    }

    @Override
    public void finish(boolean push) {

    }

//    scrollXAnimator = ObjectAnimator.ofInt(view, "scrollX", startX, endX);
}
