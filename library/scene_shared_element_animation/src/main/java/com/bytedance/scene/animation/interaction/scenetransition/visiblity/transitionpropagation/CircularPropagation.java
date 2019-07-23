package com.bytedance.scene.animation.interaction.scenetransition.visiblity.transitionpropagation;

import android.graphics.Rect;
import android.support.transition.TransitionValues;
import android.view.ViewGroup;

import com.bytedance.scene.animation.interaction.scenetransition.visiblity.SceneVisibilityTransition;

public class CircularPropagation extends VisibilityPropagation {

    private float mPropagationSpeed = 3.0f;

    /**
     * Sets the speed at which transition propagation happens, relative to the duration of the
     * Transition. A <code>propagationSpeed</code> of 1 means that a View centered farthest from
     * the epicenter and View centered at the epicenter will have a difference
     * in start delay of approximately the duration of the Transition. A speed of 2 means the
     * start delay difference will be approximately half of the duration of the transition. A
     * value of 0 is illegal, but negative values will invert the propagation.
     *
     * @param propagationSpeed The speed at which propagation occurs, relative to the duration
     *                         of the transition. A speed of 4 means it works 4 times as fast
     *                         as the duration of the transition. May not be 0.
     */
    public void setPropagationSpeed(float propagationSpeed) {
        if (propagationSpeed == 0) {
            throw new IllegalArgumentException("propagationSpeed may not be 0");
        }
        mPropagationSpeed = propagationSpeed;
    }

    @Override
    public long getStartDelay(ViewGroup sceneRoot, SceneVisibilityTransition transition, TransitionPropagationResult result, boolean appear) {
        int directionMultiplier = 1;
        TransitionValues positionValues;
        if (!appear) {
            directionMultiplier = -1;
        }

        int viewCenterX = getViewX(result);
        int viewCenterY = getViewY(result);

        Rect epicenter = transition.getEpicenter();
        int epicenterX;
        int epicenterY;
        if (epicenter != null) {
            epicenterX = epicenter.centerX();
            epicenterY = epicenter.centerY();
        } else {
            int[] loc = new int[2];
            sceneRoot.getLocationOnScreen(loc);
            epicenterX = Math.round(loc[0] + (sceneRoot.getWidth() / 2)
                    + sceneRoot.getTranslationX());
            epicenterY = Math.round(loc[1] + (sceneRoot.getHeight() / 2)
                    + sceneRoot.getTranslationY());
        }
        float distance = distance(viewCenterX, viewCenterY, epicenterX, epicenterY);
        float maxDistance = distance(0, 0, sceneRoot.getWidth(), sceneRoot.getHeight());
        float distanceFraction = distance / maxDistance;

        long duration = transition.getDuration();
        if (duration < 0) {
            duration = 300;
        }

        return Math.round(duration * directionMultiplier / mPropagationSpeed * distanceFraction);
    }

    private static float distance(float x1, float y1, float x2, float y2) {
        float x = x2 - x1;
        float y = y2 - y1;
        return (float) Math.sqrt((x * x) + (y * y));
    }
}