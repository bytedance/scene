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
package com.bytedance.scenerouter.core;

import android.support.annotation.NonNull;

import com.bytedance.scene.Scene;
import com.bytedance.scene.navigation.NavigationScene;

public final class SceneRouters {
    @NonNull
    public static SceneRouter of(@NonNull Scene scene) {
        return of(scene.requireNavigationScene());
    }

    @NonNull
    public static SceneRouter of(@NonNull NavigationScene navigationScene) {
        return new SceneRouter(navigationScene);
    }

    public static void bind(@NonNull Scene scene) {
        RouterValueBinder.bind(scene);
    }
}
