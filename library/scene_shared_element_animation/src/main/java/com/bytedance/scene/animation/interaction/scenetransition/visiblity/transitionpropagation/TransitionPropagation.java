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
package com.bytedance.scene.animation.interaction.scenetransition.visiblity.transitionpropagation;

import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.animation.interaction.scenetransition.visiblity.SceneVisibilityTransition;

/**
 * Created by JiangQi on 10/23/18.
 */
public abstract class TransitionPropagation {
    public abstract long getStartDelay(ViewGroup sceneRoot,
                                       SceneVisibilityTransition transition,
                                       TransitionPropagationResult result,
                                       boolean appear);

    public abstract TransitionPropagationResult captureValues(@NonNull View view, @NonNull ViewGroup rootView);
}
