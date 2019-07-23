package com.bytedance.scene.animation.interaction.scenetransition;

import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.util.Property;
import android.view.View;

import com.bytedance.scene.animation.interaction.progressanimation.AnimatorFactory;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.animation.interaction.scenetransition.utils.ArgbEvaluator;

public class BackgroundRecolor extends SceneTransition {
    private ArgbEvaluator mIntEvaluator = new ArgbEvaluator();
    private int mStartColor;
    private int mEndColor;

    public BackgroundRecolor() {
    }

    @Override
    public void captureValue(@NonNull View fromView, @NonNull View toView, @NonNull View animationView) {
        super.captureValue(fromView, toView, animationView);
        if (fromView.getBackground() instanceof ColorDrawable && toView.getBackground() instanceof ColorDrawable && animationView.getBackground() instanceof ColorDrawable) {
            mStartColor = ((ColorDrawable) this.mFromView.getBackground()).getColor();
            mEndColor = ((ColorDrawable) this.mToView.getBackground()).getColor();
        }
    }

    @Override
    public InteractionAnimation getAnimation(boolean push) {
        if (mStartColor != mEndColor) {
            final ColorDrawable endColorDrawable = (ColorDrawable) mAnimationView.getBackground().mutate();
            mAnimationView.setBackgroundDrawable(endColorDrawable);
            endColorDrawable.setColor(mStartColor);

            AnimatorFactory<ColorDrawable, Integer> animatorFactory = new AnimatorFactory<>(endColorDrawable,
                    new Property<ColorDrawable, Integer>(Integer.class, "backgroundColor") {
                        @Override
                        public void set(ColorDrawable object, Integer value) {
                            object.setColor(value);
                        }

                        @Override
                        public Integer get(ColorDrawable object) {
                            return object.getColor();
                        }
                    }, ArgbEvaluator.getInstance(), mStartColor, mEndColor, null);
            return animatorFactory.toInteractionAnimation();
        }
        return InteractionAnimation.EMPTY;
    }

    @Override
    public void finish(boolean push) {

    }
}