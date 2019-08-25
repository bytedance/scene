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
package com.bytedance.scene.interfaces;

import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bytedance.scene.Scene;

/**
 * Created by JiangQi on 8/1/18.
 */
public interface ChildSceneLifecycleCallbacks {
    void onSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState);

    void onSceneStarted(@NonNull Scene scene);

    void onSceneResumed(@NonNull Scene scene);

    void onSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState);

    void onScenePaused(@NonNull Scene scene);

    void onSceneStopped(@NonNull Scene scene);

    void onSceneDestroyed(@NonNull Scene scene);
}
