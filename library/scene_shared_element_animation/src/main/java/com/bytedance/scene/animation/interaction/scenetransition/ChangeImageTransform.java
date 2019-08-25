/*
 * Copyright (C) 2019 ByteDance Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bytedance.scene.animation.interaction.scenetransition;

import android.animation.TypeEvaluator;
import android.annotation.TargetApi;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Property;
import android.view.View;
import android.widget.ImageView;

import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.animation.interaction.progressanimation.AnimatorFactory;
import com.bytedance.scene.animation.interaction.scenetransition.utils.SceneViewCompatUtils;
import com.bytedance.scene.utlity.NonNullPair;

/**
 * Created by JiangQi on 10/19/18.
 */
@TargetApi(21)
public class ChangeImageTransform extends SceneTransition {
    private NonNullPair<Rect, Matrix> mFromValue;
    private NonNullPair<Rect, Matrix> mToValue;
    private static final MatrixEvaluator MATRIX_EVALUATOR = new MatrixEvaluator();

    @Override
    public void captureValue(@NonNull View fromView, @NonNull View toView, @NonNull View animationView) {
        super.captureValue(fromView, toView, animationView);
        this.mFromValue = captureValues(fromView);
        this.mToValue = captureValues(toView);
    }

    private static NonNullPair<Rect, Matrix> captureValues(View view) {
        if (!(view instanceof ImageView) || view.getVisibility() != View.VISIBLE) {
            return null;
        }
        ImageView imageView = (ImageView) view;
        Drawable drawable = imageView.getDrawable();
        if (drawable == null) {
            return null;
        }

        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        if (drawableWidth == 0 || drawableHeight == 0) {
            return null;
        }
        int left = view.getLeft();
        int top = view.getTop();
        int right = view.getRight();
        int bottom = view.getBottom();

        Rect bounds = new Rect(left, top, right, bottom);
        return NonNullPair.create(bounds, copyImageMatrix(imageView));
    }

    @Override
    public InteractionAnimation getAnimation(boolean push) {
        if (mFromValue == null || mToValue == null) {
            return InteractionAnimation.EMPTY;
        }
        if (!(mAnimationView instanceof ImageView)) {
            return InteractionAnimation.EMPTY;
        }
        boolean boundsEqual = mFromValue.first.equals(mToValue.first);
        boolean matrixEqual = mFromValue.second.equals(mToValue.second);
        if (boundsEqual && matrixEqual) {
            return InteractionAnimation.EMPTY;
        }

        AnimatorFactory<ImageView, Matrix> animatorFactory = new AnimatorFactory<>((ImageView) mAnimationView,
                new Property<ImageView, Matrix>(Matrix.class, "Matrix") {
                    @Override
                    public void set(ImageView object, Matrix value) {
                        SceneViewCompatUtils.animateTransform(object, value);
                    }

                    @Override
                    public Matrix get(ImageView object) {
                        return object.getMatrix();
                    }
                }, MATRIX_EVALUATOR, mFromValue.second, mToValue.second, null);
        return animatorFactory.toInteractionAnimation();
    }

    @Override
    public void finish(boolean push) {

    }

    private static class MatrixEvaluator implements TypeEvaluator<Matrix> {
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

    private static Matrix copyImageMatrix(ImageView view) {
        switch (view.getScaleType()) {
            case FIT_XY:
                return fitXYMatrix(view);
            case CENTER_CROP:
                return centerCropMatrix(view);
            default:
                return new Matrix(view.getImageMatrix());
        }
    }

    /**
     * Calculates the image transformation matrix for an ImageView with ScaleType FIT_XY. This
     * needs to be manually calculated as the platform does not give us the value for this case.
     */
    private static Matrix fitXYMatrix(ImageView view) {
        final Drawable image = view.getDrawable();
        final Matrix matrix = new Matrix();
        matrix.postScale(
                ((float) view.getWidth()) / image.getIntrinsicWidth(),
                ((float) view.getHeight()) / image.getIntrinsicHeight());
        return matrix;
    }

    /**
     * Calculates the image transformation matrix for an ImageView with ScaleType CENTER_CROP. This
     * needs to be manually calculated for consistent behavior across all the API levels.
     */
    private static Matrix centerCropMatrix(ImageView view) {
        final Drawable image = view.getDrawable();
        final int imageWidth = image.getIntrinsicWidth();
        final int imageViewWidth = view.getWidth();
        final float scaleX = ((float) imageViewWidth) / imageWidth;

        final int imageHeight = image.getIntrinsicHeight();
        final int imageViewHeight = view.getHeight();
        final float scaleY = ((float) imageViewHeight) / imageHeight;

        final float maxScale = Math.max(scaleX, scaleY);

        final float width = imageWidth * maxScale;
        final float height = imageHeight * maxScale;
        final int tx = Math.round((imageViewWidth - width) / 2f);
        final int ty = Math.round((imageViewHeight - height) / 2f);

        final Matrix matrix = new Matrix();
        matrix.postScale(maxScale, maxScale);
        matrix.postTranslate(tx, ty);
        return matrix;
    }
}
