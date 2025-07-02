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
import com.bytedance.scene.navigation.INavigationManager

/**
 * Navigation reuse manager
 * @param navigationManager Navigation manager
 * @param reusePool Reuse pool implementation, defaults to DefaultReusePool
 *
 * Created by zhuqingying on 2025/2/8
 * @author zhuqingying@bytedance.com
 */
class NavigationReuseManager(
    private val navigationManager: INavigationManager,
    reusePool: IReusePool? = null) {

    private val reusePool: IReusePool = reusePool ?: DefaultReusePool()

    /**
     * Get the current cache pool
     */
    fun getReusePool(): IReusePool {
        return reusePool
    }

    /**
     * Put a Scene into the cache pool
     */
    fun releaseIReuseScene(scene: IReuseScene): Boolean {
        return reusePool.releaseScene(scene)
    }

    /**
     * Get a matching Scene from the cache pool
     */
    fun reuseFromPool(strategy: ReuseBehavior): Scene? {
        return reusePool.reuseScene(strategy)
    }

    /**
     * Destroy all caches
     */
    fun clear() {
        val allScenes = reusePool.removeScenes { true }
        navigationManager.destroyReuseCache(allScenes)
        reusePool.clear()
    }

    /**
     * Get the reuse state of a Scene
     */
    fun getReuseSceneState(scene: IReuseScene): ReuseState? {
        return reusePool.getSceneState(scene)
    }
}