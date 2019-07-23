package com.bytedance.scene.animation.interaction.scenetransition;

import android.animation.IntEvaluator;
import android.util.Property;
import android.view.View;
import android.widget.TextView;

import com.bytedance.scene.animation.interaction.progressanimation.AnimatorFactory;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.animation.interaction.scenetransition.utils.ArgbEvaluator;

public class TextRecolor extends SceneTransition {
    private IntEvaluator mIntEvaluator = new IntEvaluator();
    private int mFromTextColor;
    private int mToTextColor;

    public TextRecolor() {
    }

    @Override
    public void captureValue(View fromView, View toView, View animationView) {
        super.captureValue(fromView, toView, animationView);
        if (this.mFromView instanceof TextView && toView instanceof TextView) {
            this.mFromTextColor = ((TextView) fromView).getCurrentTextColor();
            this.mToTextColor = ((TextView) toView).getCurrentTextColor();
        }
    }

    @Override
    public InteractionAnimation getAnimation(boolean push) {
        if (this.mFromTextColor != this.mToTextColor) {
            ((TextView) (mAnimationView)).setTextColor(this.mFromTextColor);
            AnimatorFactory<TextView, Integer> animatorFactory = new AnimatorFactory<>((TextView) mAnimationView,
                    new Property<TextView, Integer>(Integer.class, "textColor") {
                        @Override
                        public void set(TextView object, Integer value) {
                            object.setTextColor(value);
                        }

                        @Override
                        public Integer get(TextView object) {
                            return object.getCurrentTextColor();
                        }
                    }, ArgbEvaluator.getInstance(), mFromTextColor, mToTextColor, null);
            return animatorFactory.toInteractionAnimation();
        }
        return InteractionAnimation.EMPTY;
    }

    @Override
    public void finish(boolean push) {

    }
}