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
package com.bytedance.scene.navigation.reuse

import com.bytedance.scene.Scene

/**
 * Scene reuse pool interface that can be customized by business
 *
 * Created by jixiao on 2025/2/27
 * @author jixiao.li@bytedance.com
 */
interface IReusePool {
    /**
     * Add a scene to the reuse pool
     * @param scene Scene to be added to the pool
     * @return Whether the scene was successfully added
     */
    fun releaseScene(scene: IReuseScene): Boolean

    /**
     * Get a matching scene from the reuse pool
     * @param behavior Matching behavior
     * @return Matching scene, or null if no match
     */
    fun reuseScene(behavior: ReuseBehavior): Scene?

    /**
     * Remove all scenes from the pool that match the condition
     * @param behavior Matching behavior
     * @return List of removed scenes
     */
    fun removeScenes(behavior: ReuseBehavior): List<IReuseScene?>?

    /**
     * Get the reuse state of a scene
     * @param scene Scene
     * @return Reuse state
     */
    fun getSceneState(scene: IReuseScene): ReuseState?

    /**
     * Clear the reuse pool
     */
    fun clear()
}