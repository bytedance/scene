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

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by JiangQi on 10/25/18.
 * <p>
 * Pay attention, this method will be invoked more than once because of Scene application restore strategy, {@link NavigationSene#recycleInvisibleScenes}
 */
public interface SceneComponentFactory {
    @Nullable
    Scene instantiateScene(@NonNull ClassLoader cl, @NonNull String className, @Nullable Bundle bundle);
}
