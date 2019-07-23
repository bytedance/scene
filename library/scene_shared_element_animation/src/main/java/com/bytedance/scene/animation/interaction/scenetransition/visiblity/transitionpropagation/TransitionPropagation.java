package com.bytedance.scene.animation.interaction.scenetransition.visiblity.transitionpropagation;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.animation.interaction.scenetransition.visiblity.SceneVisibilityTransition;

/**
 * Created by JiangQi on 10/23/18.
 */
public abstract class TransitionPropagation {
    public abstract long getStartDelay(ViewGroup sceneRoot,
                                       SceneVisibilityTransition transition,
                                       TransitionPropagationResult result,
                                       boolean appear);

    public abstract TransitionPropagationResult captureValues(@NonNull View view, @NonNull ViewGroup rootView);
}
