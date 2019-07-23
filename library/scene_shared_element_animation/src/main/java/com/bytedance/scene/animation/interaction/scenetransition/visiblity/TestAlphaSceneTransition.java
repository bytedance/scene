package com.bytedance.scene.animation.interaction.scenetransition.visiblity;

import com.bytedance.scene.animation.interaction.progressanimation.AnimationBuilder;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;

public class TestAlphaSceneTransition extends SceneVisibilityTransition {

    public TestAlphaSceneTransition() {
    }

    @Override
    public InteractionAnimation getAnimation(boolean appear) {
        if (appear) {
            return AnimationBuilder.of(mView).alpha(0.0f, 1.0f).build();
        } else {
            return AnimationBuilder.of(mView).alpha(1.0f, 0.0f).build();
        }
    }

    @Override
    public void onFinish(boolean appear) {

    }
}