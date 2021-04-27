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
package com.bytedance.scene.utlity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import androidx.annotation.RestrictTo;
import android.view.View;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 8/7/18.
 */

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class EnableLayerAnimationListener extends AnimatorListenerAdapter {
    private View mView;
    private int mInitLayerType;

    public EnableLayerAnimationListener(View view) {
        this.mView = view;
        this.mInitLayerType = this.mView.getLayerType();
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        this.mView.setLayerType(this.mInitLayerType, null);
    }

    @Override
    public void onAnimationStart(Animator animation) {
        super.onAnimationStart(animation);
        this.mView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }
}
