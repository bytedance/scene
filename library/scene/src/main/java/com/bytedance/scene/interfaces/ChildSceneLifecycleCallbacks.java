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
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;

/**
 * Created by JiangQi on 8/1/18.
 * <p>
 * method invoke order:
 * 1. onPreSceneMethod(invoke before Scene.onMethod)
 * 2. onSuperSceneMethod(invoke in Scene.super.onMethod)
 * 3. onSceneMethod(invoke after Scene.onMethod)
 */
public interface ChildSceneLifecycleCallbacks {

    void onPreSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState);

    void onPreSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState);

    void onPreSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState);

    void onPreSceneStarted(@NonNull Scene scene);

    void onPreSceneResumed(@NonNull Scene scene);

    void onPreSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState);

    void onPreScenePaused(@NonNull Scene scene);

    void onPreSceneStopped(@NonNull Scene scene);

    void onPreSceneViewDestroyed(@NonNull Scene scene);

    void onPreSceneDestroyed(@NonNull Scene scene);

    void onSuperSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState);

    void onSuperSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState);

    void onSuperSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState);

    void onSuperSceneStarted(@NonNull Scene scene);

    void onSuperSceneResumed(@NonNull Scene scene);

    void onSuperSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState);

    void onSuperScenePaused(@NonNull Scene scene);

    void onSuperSceneStopped(@NonNull Scene scene);

    void onSuperSceneViewDestroyed(@NonNull Scene scene);

    void onSuperSceneDestroyed(@NonNull Scene scene);

    /**
     * use {@link #onSceneActivityCreated(Scene, Bundle)} instead
     * <p>
     * Called after the Scene has returned from {@link Scene#onCreate(Bundle)}.
     * View is not created at this moment
     *
     * @param scene              Scene changing state
     * @param savedInstanceState Saved instance bundle from a previous instance
     **/
    @Deprecated
    void onSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState);

    /**
     * Called after the Scene has returned from {@link Scene#onViewCreated(View, Bundle)}.
     * View is already created at this moment, but child Scene' view is not created.
     *
     * @param scene              Scene changing state
     * @param savedInstanceState Saved instance bundle from a previous instance
     */
    void onSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState);

    /**
     * Called after the Scene has returned from {@link Scene#onActivityCreated(Bundle)}.
     * View is already created at this moment, and child Scene' view is created too.
     *
     * @param scene              Scene changing state
     * @param savedInstanceState Saved instance bundle from a previous instance
     */
    void onSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState);

    /**
     * Called after the Scene has returned from {@link Scene#onStart()}.
     *
     * @param scene Scene changing state
     */
    void onSceneStarted(@NonNull Scene scene);

    /**
     * Called after the Scene has returned from {@link Scene#onResume()} ()}.
     *
     * @param scene Scene changing state
     */
    void onSceneResumed(@NonNull Scene scene);

    /**
     * Called after the Scene has returned from {@link Scene#onSaveInstanceState(Bundle)}.
     *
     * @param scene    Scene changing state
     * @param outState Saved state bundle for the Scene
     */
    void onSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState);

    /**
     * Called after the Scene has returned from {@link Scene#onPause()} ()}.
     *
     * @param scene Scene changing state
     */
    void onScenePaused(@NonNull Scene scene);

    /**
     * Called after the Scene has returned from {@link Scene#onStop()}}.
     *
     * @param scene Scene changing state
     */
    void onSceneStopped(@NonNull Scene scene);

    /**
     * Called after the Scene has returned from {@link Scene#onDestroyView()}.
     * View is still available at this moment
     *
     * @param scene Scene changing state
     */
    void onSceneViewDestroyed(@NonNull Scene scene);

    /**
     * Called after the Scene has returned from {@link Scene#onDestroy()}.
     * View is not available at this moment
     *
     * @param scene Scene changing state
     */
    void onSceneDestroyed(@NonNull Scene scene);
}
