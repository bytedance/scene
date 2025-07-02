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
 * Default implementation of IReusePool
 *
 * Created by zhuqingying on 2025/2/8
 * @author zhuqingying@bytedance.com
 */
class DefaultReusePool : IReusePool {
    private val reusePool: ArrayDeque<IReuseScene> = ArrayDeque()
    private val reuseStates = HashMap<IReuseScene, ReuseState>()

    override fun releaseScene(scene: IReuseScene): Boolean {
        if (!scene.isReusable() || reusePool.contains(scene)) {
            return false
        }

        reusePool.addLast(scene)
        reuseStates[scene] = ReuseState.RELEASED
        return true
    }

    override fun reuseScene(behavior: ReuseBehavior): Scene? {
        // Find the first matching scene
        val matchedScene = reusePool.find { behavior.match(it) } ?: return null

        // Remove from pool
        reusePool.remove(matchedScene)
        reuseStates[matchedScene] = ReuseState.REUSED
        return matchedScene as Scene
    }


    override fun removeScenes(behavior: ReuseBehavior): List<IReuseScene> {
        // Find all matching scenes
        val scenesToRemove = reusePool.filter { behavior.match(it) }

        // Remove them from the pool
        reusePool.removeAll(scenesToRemove.toSet())

        return scenesToRemove
    }

    override fun getSceneState(scene: IReuseScene): ReuseState {
        return reuseStates[scene] ?: ReuseState.INITED
    }

    override fun clear() {
        reusePool.clear()
        reuseStates.clear()
    }
}