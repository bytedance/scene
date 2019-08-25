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
package com.bytedance.scene.animation.interaction.scenetransition.visiblity;

import android.animation.TimeInterpolator;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.animation.interaction.scenetransition.visiblity.transitionpropagation.CircularPropagation;

//todo setPropagation(new CircularPropagation())
public class Explode extends SceneVisibilityTransition {
    private static final TimeInterpolator sDecelerate = new DecelerateInterpolator();
    private static final TimeInterpolator sAccelerate = new AccelerateInterpolator();
    private static final String TAG = "Explode";
    private static final String PROPNAME_SCREEN_BOUNDS = "android:explode:screenBounds";

    private int[] mTempLoc = new int[2];

    private Rect mRect;

    public Explode() {
        setPropagation(new CircularPropagation());
    }

    @Override
    public void captureValue(@NonNull View view, @NonNull ViewGroup rootView) {
        super.captureValue(view, rootView);

        view.getLocationOnScreen(mTempLoc);
        int left = mTempLoc[0];
        int top = mTempLoc[1];
        int right = left + view.getWidth();
        int bottom = top + view.getHeight();
        mRect = new Rect(left, top, right, bottom);
    }

    @Override
    public InteractionAnimation getAnimation(boolean appear) {
        if (appear) {
            Rect bounds = mRect;
            float endX = mView.getTranslationX();
            float endY = mView.getTranslationY();
            calculateOut(mRootView, bounds, mTempLoc);
            float startX = endX + mTempLoc[0];
            float startY = endY + mTempLoc[1];
            return TranslationAnimationCreator.createAnimation(mView, bounds.left, bounds.top,
                    startX, startY, endX, endY, sDecelerate);
        } else {
            Rect bounds = mRect;
            int viewPosX = bounds.left;
            int viewPosY = bounds.top;
            float startX = mView.getTranslationX();
            float startY = mView.getTranslationY();
            float endX = startX;
            float endY = startY;
//            int[] interruptedPosition = (int[]) startValues.view.getTag(R.id.transitionPosition);
//            if (interruptedPosition != null) {
//                // We want to have the end position relative to the interrupted position, not
//                // the position it was supposed to start at.
//                endX += interruptedPosition[0] - bounds.left;
//                endY += interruptedPosition[1] - bounds.top;
//                bounds.offsetTo(interruptedPosition[0], interruptedPosition[1]);
//            }
            calculateOut(mRootView, bounds, mTempLoc);
            endX += mTempLoc[0];
            endY += mTempLoc[1];

            return TranslationAnimationCreator.createAnimation(mView,
                    viewPosX, viewPosY, startX, startY, endX, endY, sAccelerate);
        }
    }

    private void calculateOut(View sceneRoot, Rect bounds, int[] outVector) {
        sceneRoot.getLocationOnScreen(mTempLoc);
        int sceneRootX = mTempLoc[0];
        int sceneRootY = mTempLoc[1];
        int focalX;
        int focalY;

        Rect epicenter = getEpicenter();
        if (epicenter == null) {
            focalX = sceneRootX + (sceneRoot.getWidth() / 2)
                    + Math.round(sceneRoot.getTranslationX());
            focalY = sceneRootY + (sceneRoot.getHeight() / 2)
                    + Math.round(sceneRoot.getTranslationY());
        } else {
            focalX = epicenter.centerX();
            focalY = epicenter.centerY();
        }

        int centerX = bounds.centerX();
        int centerY = bounds.centerY();
        double xVector = centerX - focalX;
        double yVector = centerY - focalY;

        if (xVector == 0 && yVector == 0) {
            // Random direction when View is centered on focal View.
            xVector = (Math.random() * 2) - 1;
            yVector = (Math.random() * 2) - 1;
        }
        double vectorSize = Math.hypot(xVector, yVector);
        xVector /= vectorSize;
        yVector /= vectorSize;

        double maxDistance =
                calculateMaxDistance(sceneRoot, focalX - sceneRootX, focalY - sceneRootY);

        outVector[0] = (int) Math.round(maxDistance * xVector);
        outVector[1] = (int) Math.round(maxDistance * yVector);
    }

    private static double calculateMaxDistance(View sceneRoot, int focalX, int focalY) {
        int maxX = Math.max(focalX, sceneRoot.getWidth() - focalX);
        int maxY = Math.max(focalY, sceneRoot.getHeight() - focalY);
        return Math.hypot(maxX, maxY);
    }


    @Override
    public void onFinish(boolean appear) {

    }
}
