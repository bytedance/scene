package com.bytedance.scene.animation.interaction.progressanimation;

import java.util.ArrayList;
import java.util.List;

public class InteractionAnimationSet extends InteractionAnimation {
    private List<InteractionAnimation> list = new ArrayList<>();

    public InteractionAnimationSet() {
        super(1.0f);
    }

    public InteractionAnimationSet addInteractionAnimation(InteractionAnimation animation) {
        list.add(animation);
        return this;
    }

    @Override
    public void onProgress(float progress) {
        for (InteractionAnimation animation : list) {
            animation.onProgress(progress);
        }
    }
}
