package com.bytedance.scene.animation.interaction.scenetransition.visiblity.transitionpropagation;

/**
 * Created by JiangQi on 10/23/18.
 */
public class TransitionPropagationResult {
    public final int mVisibility;
    public final int[] mCenter;

    public TransitionPropagationResult(int visibility, int[] center) {
        this.mVisibility = visibility;
        this.mCenter = center;
    }
}
