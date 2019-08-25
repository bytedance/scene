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

import android.graphics.drawable.Drawable;

import com.bytedance.scene.animation.interaction.progressanimation.AnimationBuilder;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;

public class BackgroundFade extends SceneVisibilityTransition {
    @Override
    public InteractionAnimation getAnimation(boolean appear) {
        if (appear && mView.getBackground() != null) {
            Drawable drawable = mView.getBackground().mutate();
            mView.setBackgroundDrawable(drawable);
            return AnimationBuilder.of(mView.getBackground()).alpha(0, 255).build();
        } else if (!appear && mView.getBackground() != null) {
            Drawable drawable = mView.getBackground().mutate();
            mView.setBackgroundDrawable(drawable);
            return AnimationBuilder.of(mView.getBackground()).alpha(255, 0).build();
        }
        return InteractionAnimation.EMPTY;
    }

    @Override
    public void onFinish(boolean appear) {

    }
}