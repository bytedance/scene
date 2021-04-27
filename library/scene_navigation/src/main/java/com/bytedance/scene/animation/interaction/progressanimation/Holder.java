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

import android.animation.TypeEvaluator;

public class Holder {
    public TypeEvaluator typeEvaluator;
    public Object fromValue;
    public Object toValue;

    public Holder(TypeEvaluator typeEvaluator, Object fromValue, Object toValue) {
        this.typeEvaluator = typeEvaluator;
        this.fromValue = fromValue;
        this.toValue = toValue;
    }
}