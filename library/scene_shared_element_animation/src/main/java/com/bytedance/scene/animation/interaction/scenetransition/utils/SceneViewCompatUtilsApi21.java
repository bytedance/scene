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
package com.bytedance.scene.animation.interaction.scenetransition.utils;

import android.annotation.TargetApi;
import android.graphics.Matrix;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by JiangQi on 10/23/18.
 */
@TargetApi(21)
class SceneViewCompatUtilsApi21 implements SceneViewCompatUtils.SceneViewCompatUtilsImpl {
    private ImageViewRefUtils mImageViewRefUtils = new ImageViewRefUtils();
    private TransitionAlphaRefUtils mTransitionAlphaRefUtils = new TransitionAlphaRefUtils();
    private SuppressLayoutUtils mSuppressLayoutUtils = new SuppressLayoutUtils();
    private TransformMatrixUtils mTransformMatrixUtils = new TransformMatrixUtils();

    @Override
    public void setLeftTopRightBottom(View v, int left, int top, int right, int bottom) {
        v.setLeft(left);
        v.setTop(top);
        v.setRight(right);
        v.setBottom(bottom);
    }

    @Override
    public void animateTransform(ImageView view, Matrix matrix) {
        mImageViewRefUtils.animateTransform(view, matrix);
    }

    @Override
    public float getTransitionAlpha(@NonNull View view) {
        return mTransitionAlphaRefUtils.getTransitionAlpha(view);
    }

    @Override
    public void setTransitionAlpha(@NonNull View view, float alpha) {
        mTransitionAlphaRefUtils.setTransitionAlpha(view, alpha);
    }

    @Override
    public void suppressLayout(@NonNull ViewGroup group, boolean suppress) {
        mSuppressLayoutUtils.suppressLayout(group, suppress);
    }

    @Override
    public void transformMatrixToGlobal(@NonNull View view, @NonNull Matrix matrix) {
        mTransformMatrixUtils.transformMatrixToGlobal(view, matrix);
    }

    @Override
    public void transformMatrixToLocal(@NonNull View view, @NonNull Matrix matrix) {
        mTransformMatrixUtils.transformMatrixToLocal(view, matrix);
    }

    @Override
    public void setAnimationMatrix(@NonNull View view, Matrix matrix) {
        mTransformMatrixUtils.setAnimationMatrix(view, matrix);
    }
}
