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
package com.bytedance.scene;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bytedance.scene.navigation.NavigationScene;

public interface SceneDelegate {
    boolean onBackPressed();

    @Nullable
    NavigationScene getNavigationScene();

    /**
     * The timing of creating NavigationScene is different:
     *   1. the first time bind to Fragment
     *   2. Fragment destruction recovery
     * This method can guarantee when the NavigationScene object is created.
     * Notice: The callback doesn't have the complete life cycle,
     *         it's just a primitive Java object.
     */
    void setNavigationSceneAvailableCallback(@NonNull NavigationSceneAvailableCallback callback);

    //clear all lifecycle fragment, scope fragment, NavigationScene, GroupScene relation to this SceneDelegate
    void abandon();
}