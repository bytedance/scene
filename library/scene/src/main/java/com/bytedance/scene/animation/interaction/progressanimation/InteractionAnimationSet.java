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
