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