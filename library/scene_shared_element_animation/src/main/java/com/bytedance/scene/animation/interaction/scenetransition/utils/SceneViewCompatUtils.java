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
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by JiangQi on 10/23/18.
 */
@TargetApi(21)
public class SceneViewCompatUtils {
    interface SceneViewCompatUtilsImpl {
        void setLeftTopRightBottom(View v, int left, int top, int right, int bottom);

        void animateTransform(ImageView view, Matrix matrix);

        float getTransitionAlpha(@NonNull View view);

        void setTransitionAlpha(@NonNull View view, float alpha);

        void suppressLayout(@NonNull ViewGroup group, boolean suppress);

        void transformMatrixToGlobal(@NonNull View view, @NonNull Matrix matrix);

        void transformMatrixToLocal(@NonNull View view, @NonNull Matrix matrix);

        void setAnimationMatrix(@NonNull View view, Matrix matrix);
    }

    private static final SceneViewCompatUtilsImpl IMPL;

    static {
        if (Build.VERSION.SDK_INT >= 22) {
            IMPL = new SceneViewCompatUtilsApi22();
        } else {
            IMPL = new SceneViewCompatUtilsApi21();
        }
    }

    public static void setLeftTopRightBottom(View v, int left, int top, int right, int bottom) {
        IMPL.setLeftTopRightBottom(v, left, top, right, bottom);
    }

    public static void animateTransform(ImageView view, Matrix matrix) {
        IMPL.animateTransform(view, matrix);
    }

    public static float getTransitionAlpha(@NonNull View view) {
        return IMPL.getTransitionAlpha(view);
    }

    public static void setTransitionAlpha(@NonNull View view, float alpha) {
        IMPL.setTransitionAlpha(view, alpha);
    }

    public static void suppressLayout(@NonNull ViewGroup group, boolean suppress) {
        IMPL.suppressLayout(group, suppress);
    }

    public static void transformMatrixToGlobal(@NonNull View view, @NonNull Matrix matrix) {
        IMPL.transformMatrixToGlobal(view, matrix);
    }

    public static void transformMatrixToLocal(@NonNull View view, @NonNull Matrix matrix) {
        IMPL.transformMatrixToLocal(view, matrix);
    }

    public static void setAnimationMatrix(@NonNull View view, Matrix matrix) {
        IMPL.setAnimationMatrix(view, matrix);
    }
}
