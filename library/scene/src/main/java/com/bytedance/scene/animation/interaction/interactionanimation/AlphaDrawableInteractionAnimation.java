package com.bytedance.scene.animation.interaction.interactionanimation;

import android.animation.FloatEvaluator;
import android.graphics.drawable.Drawable;

import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;

/**
 * Created by JiangQi on 8/22/18.
 */
public class AlphaDrawableInteractionAnimation extends InteractionAnimation {
    private FloatEvaluator evaluator = new FloatEvaluator();
    private Drawable mDrawable;
    private float mFromValue;
    private float mToValue;

    public AlphaDrawableInteractionAnimation(Drawable drawable, float fromValue, float toValue, float endProgress) {
        super(endProgress);
        this.mDrawable = drawable;
        this.mFromValue = fromValue;
        this.mToValue = toValue;
    }

    @Override
    public void onProgress(float progress) {
        mDrawable.setAlpha((int) (255 * evaluator.evaluate(progress, this.mFromValue, this.mToValue)));
        mDrawable.invalidateSelf();
    }
}
