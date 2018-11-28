package com.bytedance.scene.animation.interaction.interactionanimation;

import android.animation.FloatEvaluator;
import android.util.Property;
import android.view.View;

import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;

/**
 * Created by JiangQi on 8/9/18.
 */
public class TranslationXInteractionAnimation extends InteractionAnimation {
    private FloatEvaluator evaluator = new FloatEvaluator();
    private Property<View, Float> property = View.TRANSLATION_X;
    private View mTargetView;
    private float mFromValue;
    private float mToValue;

    public TranslationXInteractionAnimation(View targetView, float fromValue, float toValue, float endProgress) {
        super(endProgress);
        this.mTargetView = targetView;
        this.mFromValue = fromValue;
        this.mToValue = toValue;
    }

    @Override
    public void onProgress(float progress) {
        property.set(mTargetView, evaluator.evaluate(progress, this.mFromValue, this.mToValue));
    }
}
