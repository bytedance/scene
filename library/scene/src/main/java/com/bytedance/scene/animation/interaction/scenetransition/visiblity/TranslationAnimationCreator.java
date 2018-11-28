package com.bytedance.scene.animation.interaction.scenetransition.visiblity;

import android.animation.TimeInterpolator;
import android.graphics.Path;
import android.support.transition.Transition;
import android.support.transition.TransitionValues;
import android.view.View;
import com.bytedance.scene.animation.interaction.progressanimation.PathInteractionAnimation;

class TranslationAnimationCreator {

    /**
     * Creates an animator that can be used for x and/or y translations. When interrupted,
     * it sets a tag to keep track of the position so that it may be continued from position.
     *
     * @param view         The view being moved. This may be in the overlay for onDisappear.
     * @param values       The values containing the view in the view hierarchy.
     * @param viewPosX     The x screen coordinate of view
     * @param viewPosY     The y screen coordinate of view
     * @param startX       The start translation x of view
     * @param startY       The start translation y of view
     * @param endX         The end translation x of view
     * @param endY         The end translation y of view
     * @param interpolator The interpolator to use with this animator.
     * @return An animator that moves from (startX, startY) to (endX, endY) unless there was
     * a previous interruption, in which case it moves from the current position to (endX, endY).
     */
    static PathInteractionAnimation createAnimation(View view, int viewPosX, int viewPosY,
                                                    float startX, float startY, float endX, float endY, TimeInterpolator interpolator) {
        float terminalX = view.getTranslationX();
        float terminalY = view.getTranslationY();

        // Initial position is at translation startX, startY, so position is offset by that amount
        int startPosX = viewPosX + Math.round(startX - terminalX);
        int startPosY = viewPosY + Math.round(startY - terminalY);

        view.setTranslationX(startX);
        view.setTranslationY(startY);
        if (startX == endX && startY == endY) {
            return null;
        }
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);

        return new PathInteractionAnimation(1.0f, path, view);
    }
}