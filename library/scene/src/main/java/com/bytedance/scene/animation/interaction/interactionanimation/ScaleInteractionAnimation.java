package com.bytedance.scene.animation.interaction.interactionanimation;

import android.animation.FloatEvaluator;
import android.util.Property;
import android.view.View;

import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;

/**
 * Created by JiangQi on 8/22/18.
 */
public class ScaleInteractionAnimation extends InteractionAnimation {
    private FloatEvaluator evaluator = new FloatEvaluator();
    private Property<View, Float> xProperty = View.SCALE_X;
    private Property<View, Float> yProperty = View.SCALE_Y;

    private View mTargetView;
    private float mFromValue;
    private float mToValue;

    public ScaleInteractionAnimation(View targetView, float fromValue, float toValue, float endProgress) {
        super(endProgress);
        this.mTargetView = targetView;
        this.mFromValue = fromValue;
        this.mToValue = toValue;
    }

    @Override
    public void onProgress(float progress) {
        xProperty.set(mTargetView, evaluator.evaluate(progress, this.mFromValue, this.mToValue));
        yProperty.set(mTargetView, evaluator.evaluate(progress, this.mFromValue, this.mToValue));
    }
}