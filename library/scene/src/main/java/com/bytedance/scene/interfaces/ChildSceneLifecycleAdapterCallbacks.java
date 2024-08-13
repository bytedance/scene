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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bytedance.scene.Scene;

/**
 * Created by JiangQi on 8/1/18.
 */
public class ChildSceneLifecycleAdapterCallbacks implements ChildSceneLifecycleCallbacks {

    @Override
    public void onPreSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onPreSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onPreSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onPreSceneStarted(@NonNull Scene scene) {

    }

    @Override
    public void onPreSceneResumed(@NonNull Scene scene) {

    }

    @Override
    public void onPreSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState) {

    }

    @Override
    public void onPreScenePaused(@NonNull Scene scene) {

    }

    @Override
    public void onPreSceneStopped(@NonNull Scene scene) {

    }

    @Override
    public void onPreSceneViewDestroyed(@NonNull Scene scene) {

    }

    @Override
    public void onPreSceneDestroyed(@NonNull Scene scene) {

    }

    /**
     * use {@link #onSceneActivityCreated(Scene, Bundle)} instead
     */
    @Deprecated
    @Override
    public void onSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onSceneStarted(@NonNull Scene scene) {

    }

    @Override
    public void onSceneResumed(@NonNull Scene scene) {

    }

    @Override
    public void onSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState) {

    }

    @Override
    public void onScenePaused(@NonNull Scene scene) {

    }

    @Override
    public void onSceneStopped(@NonNull Scene scene) {

    }

    @Override
    public void onSceneViewDestroyed(@NonNull Scene scene) {

    }

    @Override
    public void onSceneDestroyed(@NonNull Scene scene) {

    }
}
