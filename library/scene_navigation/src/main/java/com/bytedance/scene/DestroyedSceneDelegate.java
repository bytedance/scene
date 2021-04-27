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


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.bytedance.scene.navigation.NavigationScene;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;


/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class DestroyedSceneDelegate implements SceneDelegate {
    @NonNull
    private final NavigationScene mNavigationScene;

    public DestroyedSceneDelegate(@NonNull NavigationScene navigationScene) {
        this.mNavigationScene = navigationScene;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Nullable
    @Override
    public NavigationScene getNavigationScene() {
        return this.mNavigationScene;
    }

    @Override
    public void setNavigationSceneAvailableCallback(@NonNull NavigationSceneAvailableCallback callback) {

    }

    @Override
    public void abandon() {

    }
}