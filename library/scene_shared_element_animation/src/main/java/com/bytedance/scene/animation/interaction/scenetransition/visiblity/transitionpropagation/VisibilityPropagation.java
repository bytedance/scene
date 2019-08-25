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
package com.bytedance.scene.animation.interaction.scenetransition.visiblity.transitionpropagation;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by JiangQi on 10/23/18.
 */
public abstract class VisibilityPropagation extends TransitionPropagation {
    @Override
    public TransitionPropagationResult captureValues(@NonNull View view, @NonNull ViewGroup rootView) {
        Integer visibility = view.getVisibility();
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        loc[0] += Math.round(view.getTranslationX());
        loc[0] += view.getWidth() / 2;
        loc[1] += Math.round(view.getTranslationY());
        loc[1] += view.getHeight() / 2;
        return new TransitionPropagationResult(visibility, loc);
    }

    /**
     * Returns {@link android.view.View#getVisibility()} for the View at the time the values
     * were captured.
     *
     * @param values The TransitionValues captured at the start or end of the Transition.
     * @return {@link android.view.View#getVisibility()} for the View at the time the values
     * were captured.
     */
    public int getViewVisibility(TransitionPropagationResult values) {
        if (values == null) {
            return View.GONE;
        }
        Integer visibility = (Integer) values.mVisibility;
        if (visibility == null) {
            return View.GONE;
        }
        return visibility;
    }

    /**
     * Returns the View's center x coordinate, relative to the screen, at the time the values
     * were captured.
     *
     * @param values The TransitionValues captured at the start or end of the Transition.
     * @return the View's center x coordinate, relative to the screen, at the time the values
     * were captured.
     */
    public int getViewX(TransitionPropagationResult values) {
        return getViewCoordinate(values, 0);
    }

    /**
     * Returns the View's center y coordinate, relative to the screen, at the time the values
     * were captured.
     *
     * @param values The TransitionValues captured at the start or end of the Transition.
     * @return the View's center y coordinate, relative to the screen, at the time the values
     * were captured.
     */
    public int getViewY(TransitionPropagationResult values) {
        return getViewCoordinate(values, 1);
    }

    private static int getViewCoordinate(TransitionPropagationResult values, int coordinateIndex) {
        if (values == null) {
            return -1;
        }

        int[] coordinates = (int[]) values.mCenter;
        if (coordinates == null) {
            return -1;
        }

        return coordinates[coordinateIndex];
    }

}
