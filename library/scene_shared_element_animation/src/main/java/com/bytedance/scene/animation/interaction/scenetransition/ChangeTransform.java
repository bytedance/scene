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

import android.graphics.Matrix;
import android.os.Build;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;

import com.bytedance.scene.animation.interaction.evaluator.MatrixEvaluator;
import com.bytedance.scene.animation.interaction.progressanimation.AnimatorFactory;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.animation.interaction.scenetransition.utils.SceneViewCompatUtils;

/**
 * Created by JiangQi on 9/2/18.
 *
 * TODO: 1. It is not resolved temporarily: Parent clip by the rotation
 *       2. Use Path in transform
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ChangeTransform extends SceneTransition {
    private Pair<Float, Float> mDeltaTranslationX;
    private Pair<Float, Float> mDeltaTranslationY;
    private Pair<Float, Float> mDeltaTranslationZ;

    private Pair<Float, Float> mAlpha;

    private Pair<Float, Float> mRotate;
    private Pair<Float, Float> mRotateX;
    private Pair<Float, Float> mRotateY;

    private float mDstRotate;

    private Matrix mFromMatrix;
    private Matrix mToMatrix;

    private Transforms mTransforms;

    @Override
    public void captureValue(@NonNull View fromView, @NonNull View toView, @NonNull View animationView) {
        super.captureValue(fromView, toView, animationView);
        this.mTransforms = new Transforms(animationView);

        mDeltaTranslationX = Pair.create(fromView.getTranslationX(), toView.getTranslationX());
        mDeltaTranslationY = Pair.create(fromView.getTranslationY(), toView.getTranslationY());
        mDeltaTranslationZ = Pair.create(fromView.getTranslationZ(), toView.getTranslationZ());

        mAlpha = Pair.create(fromView.getAlpha(), toView.getAlpha());

        //返回
        if (fromView == animationView) {
            ViewGroup mFromViewParent = (ViewGroup) mFromView.getParent();
            ViewGroup mToViewParent = (ViewGroup) mToView.getParent();

            Matrix fromViewParentMatrix = new Matrix();
            SceneViewCompatUtils.transformMatrixToGlobal(mFromViewParent, fromViewParentMatrix);
            Matrix toViewParentMatrix = new Matrix();
            SceneViewCompatUtils.transformMatrixToGlobal(mToViewParent, toViewParentMatrix);

            Matrix mFromViewMatrix = new Matrix(mFromView.getMatrix());
            Matrix mToViewMatrix = new Matrix(mToView.getMatrix());

            Matrix toLocal = new Matrix();
            fromViewParentMatrix.invert(toLocal);

            mToViewMatrix.postTranslate(toView.getLeft(), toView.getTop());
            mToViewMatrix.postConcat(toViewParentMatrix);
            mToViewMatrix.postConcat(toLocal);
            mToViewMatrix.postTranslate(-fromView.getLeft(), -fromView.getTop());//因为GhostView一上来就有一定的移动

            this.mFromMatrix = mFromViewMatrix;
            this.mToMatrix = mToViewMatrix;

            setIdentityTransforms(mAnimationView);
        } else {
            ViewGroup mFromViewParent = (ViewGroup) mFromView.getParent();
            ViewGroup mToViewParent = (ViewGroup) mToView.getParent();

            Matrix fromViewParentMatrix = new Matrix();
            SceneViewCompatUtils.transformMatrixToGlobal(mFromViewParent, fromViewParentMatrix);
            Matrix toViewParentMatrix = new Matrix();
            SceneViewCompatUtils.transformMatrixToGlobal(mToViewParent, toViewParentMatrix);

            Matrix mFromViewMatrix = new Matrix(mFromView.getMatrix());
            Matrix mToViewMatrix = new Matrix(mToView.getMatrix());

            Matrix toLocal = new Matrix();
            toViewParentMatrix.invert(toLocal);

            mFromViewMatrix.postTranslate(fromView.getLeft(), fromView.getTop());
            mFromViewMatrix.postConcat(fromViewParentMatrix);
            mFromViewMatrix.postConcat(toLocal);
            mFromViewMatrix.postTranslate(-toView.getLeft(), -toView.getTop());

            this.mFromMatrix = mFromViewMatrix;
            this.mToMatrix = mToViewMatrix;

            setIdentityTransforms(mAnimationView);
        }

        SceneViewCompatUtils.setAnimationMatrix(mAnimationView, mFromMatrix);

//        mRotate = Pair.create(aaa(fromMatrix), aaa(toMatrix));
        mRotateX = Pair.create(fromView.getRotationX(), toView.getRotationX());
        mRotateY = Pair.create(fromView.getRotationY(), toView.getRotationY());
        mDstRotate = toView.getRotation();
    }

    private float aaa(Matrix matrix) {
        float[] v = new float[9];
        matrix.getValues(v);
        float rAngle = Math.round(Math.atan2(v[Matrix.MSKEW_X], v[Matrix.MSCALE_X]) * (180 / Math.PI));
        return -rAngle;
    }

    public static float scaleValue(Matrix matrix) {
        float[] f = new float[9];
        matrix.getValues(f);

        float scaleX = f[Matrix.MSCALE_X];
        float scaleY = f[Matrix.MSCALE_Y];
        return scaleX;
    }

    public static float[] translateValue(Matrix matrix) {
        float[] f = new float[9];
        matrix.getValues(f);

        float translateX = f[Matrix.MTRANS_X];
        float translateY = f[Matrix.MTRANS_Y];
        return new float[]{translateX, translateY};
    }

    @Override
    public InteractionAnimation getAnimation(boolean push) {
        AnimatorFactory<View, Matrix> animatorFactory = new AnimatorFactory<>(mAnimationView, new Property<View, Matrix>(Matrix.class, "matrix") {
            @Override
            public void set(View object, Matrix value) {
                SceneViewCompatUtils.setAnimationMatrix(object, value);
            }

            @Override
            public Matrix get(View object) {
                return null;
            }
        }, new MatrixEvaluator(), mFromMatrix, mToMatrix, null);
        return animatorFactory.toInteractionAnimation();

//        return AnimationBuilder.of(mAnimationView)
//                .translationX(mDeltaTranslationX.first, mDeltaTranslationX.second)
//                .translationY(mDeltaTranslationY.first, mDeltaTranslationY.second)
//                .translationZ(mDeltaTranslationZ.first, mDeltaTranslationZ.second)
//                .scaleX(mScaleX.first, mScaleX.second)
//                .scaleY(mScaleY.first, mScaleY.second)
//                .alpha(mAlpha.first, mAlpha.second)
//                .rotation(mRotate.first, mRotate.second)
//                .rotationX(mRotateX.first, mRotateX.second)
//                .rotationY(mRotateY.first, mRotateY.second)
//                .build();
    }

    @Override
    public void finish(boolean push) {
        SceneViewCompatUtils.setAnimationMatrix(mAnimationView, null);
        mTransforms.restore(mAnimationView);
    }

    private static void setIdentityTransforms(View view) {
        setTransforms(view, 0, 0, 0, 1, 1, 0, 0, 0);
    }

    private static void setTransforms(View view, float translationX, float translationY,
                                      float translationZ, float scaleX, float scaleY, float rotationX,
                                      float rotationY, float rotationZ) {
        view.setTranslationX(translationX);
        view.setTranslationY(translationY);
        ViewCompat.setTranslationZ(view, translationZ);
        view.setScaleX(scaleX);
        view.setScaleY(scaleY);
        view.setRotationX(rotationX);
        view.setRotationY(rotationY);
        view.setRotation(rotationZ);
    }

    private static class Transforms {
        final float mTranslationX;
        final float mTranslationY;
        final float mTranslationZ;
        final float mScaleX;
        final float mScaleY;
        final float mRotationX;
        final float mRotationY;
        final float mRotationZ;

        Transforms(View view) {
            mTranslationX = view.getTranslationX();
            mTranslationY = view.getTranslationY();
            mTranslationZ = ViewCompat.getTranslationZ(view);
            mScaleX = view.getScaleX();
            mScaleY = view.getScaleY();
            mRotationX = view.getRotationX();
            mRotationY = view.getRotationY();
            mRotationZ = view.getRotation();
        }

        public void restore(View view) {
            setTransforms(view, mTranslationX, mTranslationY, mTranslationZ, mScaleX, mScaleY,
                    mRotationX, mRotationY, mRotationZ);
        }

        @Override
        public boolean equals(Object that) {
            if (!(that instanceof Transforms)) {
                return false;
            }
            Transforms thatTransform = (Transforms) that;
            return thatTransform.mTranslationX == mTranslationX
                    && thatTransform.mTranslationY == mTranslationY
                    && thatTransform.mTranslationZ == mTranslationZ
                    && thatTransform.mScaleX == mScaleX
                    && thatTransform.mScaleY == mScaleY
                    && thatTransform.mRotationX == mRotationX
                    && thatTransform.mRotationY == mRotationY
                    && thatTransform.mRotationZ == mRotationZ;
        }

        @Override
        public int hashCode() {
            int code = mTranslationX != +0.0f ? Float.floatToIntBits(mTranslationX) : 0;
            code = 31 * code + (mTranslationY != +0.0f ? Float.floatToIntBits(mTranslationY) : 0);
            code = 31 * code + (mTranslationZ != +0.0f ? Float.floatToIntBits(mTranslationZ) : 0);
            code = 31 * code + (mScaleX != +0.0f ? Float.floatToIntBits(mScaleX) : 0);
            code = 31 * code + (mScaleY != +0.0f ? Float.floatToIntBits(mScaleY) : 0);
            code = 31 * code + (mRotationX != +0.0f ? Float.floatToIntBits(mRotationX) : 0);
            code = 31 * code + (mRotationY != +0.0f ? Float.floatToIntBits(mRotationY) : 0);
            code = 31 * code + (mRotationZ != +0.0f ? Float.floatToIntBits(mRotationZ) : 0);
            return code;
        }
    }
}
