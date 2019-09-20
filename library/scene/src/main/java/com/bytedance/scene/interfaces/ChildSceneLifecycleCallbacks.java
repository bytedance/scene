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
     * Called after the Scene has returned from {@link Scene#onActivityCreated(Bundle)}.
     * View is already created at this moment.
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
