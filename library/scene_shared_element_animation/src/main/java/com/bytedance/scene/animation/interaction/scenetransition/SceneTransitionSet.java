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
package com.bytedance.scene.animation.interaction.scenetransition;

import android.animation.Animator;
import android.view.View;

import com.bytedance.scene.animation.TransitionUtils;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimationSet;

import java.util.ArrayList;
import java.util.List;

public class SceneTransitionSet extends SceneTransition {
    private ArrayList<SceneTransition> mTransitions = new ArrayList<>();

    @Override
    public void captureValue(View fromView, View toView, View animationView) {
        super.captureValue(fromView, toView, animationView);
        for (SceneTransition sceneTransition : mTransitions) {
            sceneTransition.captureValue(fromView, toView, animationView);
        }
    }

    public SceneTransitionSet addSceneTransition(SceneTransition sceneTransition) {
        this.mTransitions.add(sceneTransition);
        return this;
    }

    public SceneTransitionSet removeSceneTransition(SceneTransition sceneTransition) {
        this.mTransitions.remove(sceneTransition);
        return this;
    }

    @Override
    public InteractionAnimation getAnimation(boolean push) {
        InteractionAnimationSet InteractionAnimationSet = new InteractionAnimationSet();
        for (SceneTransition sceneTransition : mTransitions) {
            InteractionAnimationSet.addInteractionAnimation(sceneTransition.getAnimation(push));
        }
        return InteractionAnimationSet;
    }

    @Override
    public Animator getAnimator(boolean appear) {
        List<Animator> animatorList = new ArrayList<>();
        for (SceneTransition sceneTransition : mTransitions) {
            animatorList.add(sceneTransition.getAnimator(appear));
        }
        return TransitionUtils.mergeAnimators(animatorList);
    }

    @Override
    public void finish(boolean push) {
        for (SceneTransition sceneTransition : mTransitions) {
            sceneTransition.finish(push);
        }
    }
}
