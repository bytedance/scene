package com.bytedance.scene.animation.interaction.scenetransition;

public class AutoSceneTransition extends SceneTransitionSet {
    public AutoSceneTransition() {
        addSceneTransition(new ChangeBounds());
        addSceneTransition(new ChangeTransform());
        addSceneTransition(new ChangeImageTransform());
    }
}
