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
package com.bytedance.scene.animation;

import android.view.View;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;

public class AnimationInfo {
    public final Class<?> mSceneClass;
    public final View mSceneView;
    public final State mSceneState;
    public final boolean mIsTranslucent;

    public AnimationInfo(Scene scene, View view, State state, boolean isTranslucent) {
        this.mSceneClass = scene.getClass();
        this.mSceneView = view;
        this.mSceneState = state;
        this.mIsTranslucent = isTranslucent;
    }
}