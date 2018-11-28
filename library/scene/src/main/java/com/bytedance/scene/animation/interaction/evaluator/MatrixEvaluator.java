package com.bytedance.scene.animation.interaction.evaluator;

import android.animation.TypeEvaluator;
import android.graphics.Matrix;

public class MatrixEvaluator implements TypeEvaluator<Matrix> {
    private float[] mTempStartValues = new float[9];
    private float[] mTempEndValues = new float[9];
    private Matrix mTempMatrix = new Matrix();

    @Override
    public Matrix evaluate(float fraction, Matrix startValue, Matrix endValue) {
        startValue.getValues(mTempStartValues);
        endValue.getValues(mTempEndValues);
        for (int i = 0; i < 9; i++) {
            float diff = mTempEndValues[i] - mTempStartValues[i];
            mTempEndValues[i] = mTempStartValues[i] + (fraction * diff);
        }
        mTempMatrix.setValues(mTempEndValues);

        return mTempMatrix;
    }
}