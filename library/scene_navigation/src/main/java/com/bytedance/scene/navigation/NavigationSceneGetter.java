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
package com.bytedance.scene.navigation;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bytedance.scene.Scene;
import com.bytedance.scene.utlity.ThreadUtility;

/**
 * Traverse the Scene's ancestor until it finds NavigationScene
 */
public final class NavigationSceneGetter {
    private NavigationSceneGetter() {
        //empty
    }

    @Nullable
    public static NavigationScene getNavigationScene(@NonNull final Scene scene) {
        ThreadUtility.checkUIThread();
        return findNavigationScene(scene);
    }

    @Nullable
    private static NavigationScene findNavigationScene(@Nullable Scene scene) {
        while (scene != null) {
            Scene parentScene = scene.getParentScene();
            if (parentScene instanceof NavigationScene) {
                return (NavigationScene) parentScene;
            }
            scene = parentScene;
        }
        return null;
    }

    @NonNull
    public static NavigationScene requireNavigationScene(@NonNull Scene scene) {
        ThreadUtility.checkUIThread();
        NavigationScene navigationScene = getNavigationScene(scene);
        if (navigationScene == null) {
            Context context = scene.getApplicationContext();
            if (context == null) {
                throw new IllegalStateException("Scene " + scene + " is not attached to any Scene");
            } else if (scene instanceof NavigationScene) {
                throw new IllegalStateException("Scene " + scene + " is root Scene");
            } else {
                throw new IllegalStateException("The root of the Scene hierarchy is not NavigationScene");
            }
        }
        return navigationScene;
    }
}
