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

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AnimationBuilder {
    public static DDD of(View view) {
        return new DDD(view);
    }

    public static class DDD extends ViewOtherAnimationBuilder<DDD> {
        DDD(View view) {
            super(view);
        }
    }

    public static ImageViewAnimationBuilder of(ImageView view) {
        return new ImageViewAnimationBuilder(view);
    }

    public static TextViewAnimationBuilder of(TextView view) {
        return new TextViewAnimationBuilder(view);
    }

    public static DrawableAnimationBuilder of(Drawable drawable) {
        return new DrawableAnimationBuilder(drawable);
    }
}
