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

import android.animation.IntEvaluator;
import android.graphics.drawable.Drawable;
import android.util.Property;
import android.view.View;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by JiangQi on 9/2/18.
 */
public class DrawableAnimationBuilder {
    public static DrawableAnimationBuilder with(Drawable drawable) {
        return new DrawableAnimationBuilder(drawable);
    }

    private Drawable mDrawable;
    private float mEndProgress = 1.0f;
    private HashMap<Property, Holder> hashMap = new HashMap<>();

    private static Property<Drawable, Integer> property = new Property<Drawable, Integer>(Integer.class, "drawable_alpha") {
        @Override
        public Integer get(Drawable object) {
            return object.getAlpha();
        }

        @Override
        public void set(Drawable object, Integer value) {
            object.setAlpha(value);
        }
    };

    DrawableAnimationBuilder(Drawable drawable) {
        this.mDrawable = drawable;
    }

    public DrawableAnimationBuilder alpha(int fromValue, int toValue) {
        hashMap.put(property, new Holder(new IntEvaluator(), fromValue, toValue));
        return this;
    }

    public DrawableAnimationBuilder alphaBy(int deltaValue) {
        return alpha(mDrawable.getAlpha() + deltaValue);
    }

    public DrawableAnimationBuilder alpha(int value) {
        return alpha(mDrawable.getAlpha(), value);
    }

    public DrawableAnimationBuilder endProgress(float endProgress) {
        this.mEndProgress = endProgress;
        return this;
    }

    public InteractionAnimation build() {
        return new InteractionAnimation(this.mEndProgress) {
            @Override
            public void onProgress(float progress) {
                Set<Property> set = hashMap.keySet();
                for (Property property : set) {
                    Holder value = hashMap.get(property);
                    property.set(mDrawable, value.typeEvaluator.evaluate(progress, value.fromValue, value.toValue));
                }
            }
        };
    }
}
