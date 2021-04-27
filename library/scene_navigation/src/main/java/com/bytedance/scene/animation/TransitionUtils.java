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
package com.bytedance.scene.animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

import java.util.Arrays;
import java.util.List;

public class TransitionUtils {
    public static Animator mergeAnimators(Animator... animator) {
        return mergeAnimators(Arrays.asList(animator));
    }

    public static Animator mergeAnimators(List<Animator> animator) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        return animatorSet;
    }

    public static Bitmap viewToBitmap(View view) {
        if (view.getWidth() == 0 || view.getHeight() == 0) {
            return null;
        }
        final int width = view.getWidth();
        final int height = view.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}
