package com.bytedance.scene.animation.interaction.progressanimation;

import android.animation.TypeEvaluator;

public class Holder {
    public TypeEvaluator typeEvaluator;
    public Object fromValue;
    public Object toValue;

    public Holder(TypeEvaluator typeEvaluator, Object fromValue, Object toValue) {
        this.typeEvaluator = typeEvaluator;
        this.fromValue = fromValue;
        this.toValue = toValue;
    }
}