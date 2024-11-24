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

import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.view.NavigationFrameLayout;

/**
 * Created by JiangQi on 8/2/18.
 */
public class AnimatorUtility {
    public static void resetViewStatus(@NonNull View view) {
        view.setTranslationX(0);
        view.setTranslationY(0);
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);
        view.setRotation(0.0f);
        view.setRotationX(0.0f);
        view.setRotationY(0.0f);
        view.setAlpha(1.0f);
        view.clearAnimation();
    }

    @NonNull
    public static AnimatorInfo captureViewStatus(@NonNull View view) {
        return new AnimatorInfo(view.getTranslationX(),
                view.getTranslationY(),
                view.getScaleX(),
                view.getScaleY(),
                view.getRotation(),
                view.getRotationX(),
                view.getRotationY(),
                view.getAlpha());
    }

    public static void resetViewStatus(@NonNull View view, @NonNull AnimatorInfo animatorInfo) {
        view.setTranslationX(animatorInfo.translationX);
        view.setTranslationY(animatorInfo.translationY);
        view.setScaleX(animatorInfo.scaleX);
        view.setScaleY(animatorInfo.scaleY);
        view.setRotation(animatorInfo.rotation);
        view.setRotationX(animatorInfo.rotationX);
        view.setRotationY(animatorInfo.rotationY);
        view.setAlpha(animatorInfo.alpha);
    }

    private static void bringToFrontIfNeeded(@NonNull View view) {
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        int childCount = viewGroup.getChildCount();
        int childIndex = viewGroup.indexOfChild(view);
        if (childIndex >= 0 && childIndex != childCount - 1) {
            view.bringToFront();
        }
    }

    public static void bringAnimationViewToFrontIfNeeded(@NonNull NavigationScene navigationScene) {
        if (navigationScene.getNavigationSceneOptions().getMergeNavigationSceneView()) {
            ((NavigationFrameLayout) navigationScene.getView()).setDrawAnimationViewToFront(true);
        } else {
            bringToFrontIfNeeded(navigationScene.getAnimationContainer());
        }
    }

    public static void bringSceneViewToFrontIfNeeded(@NonNull NavigationScene navigationScene) {
        if (navigationScene.getNavigationSceneOptions().getMergeNavigationSceneView()) {
            ((NavigationFrameLayout) navigationScene.getView()).setDrawAnimationViewToFront(false);
        } else {
            bringToFrontIfNeeded(navigationScene.getSceneContainer());
        }
    }

    public static class AnimatorInfo {
        public final float translationX;
        public final float translationY;
        public final float scaleX;
        public final float scaleY;
        public final float rotation;
        public final float rotationX;
        public final float rotationY;
        public final float alpha;

        public AnimatorInfo(float translationX, float translationY,
                            float scaleX, float scaleY,
                            float rotation, float rotationX, float rotationY,
                            float alpha) {
            this.translationX = translationX;
            this.translationY = translationY;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.rotation = rotation;
            this.rotationX = rotationX;
            this.rotationY = rotationY;
            this.alpha = alpha;
        }
    }
}
