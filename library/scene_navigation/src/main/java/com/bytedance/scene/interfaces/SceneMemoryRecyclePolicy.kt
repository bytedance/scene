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
package com.bytedance.scene.interfaces

/**
 * Created by jiangqi on 2025/3/3
 * @author jiangqi@bytedance.com
 */
interface SceneMemoryRecyclePolicy {
    /**
     * if Scene follow Activity lifecycle, it will not be recycled even when app memory is lower than [com.bytedance.scene.navigation.NavigationSceneOptions.getAutoRecycleInvisibleSceneThreshold]
     * this behaviour is controlled under [com.bytedance.scene.navigation.NavigationScene.setRestoreStateInLifecycle]
     */
    fun followActivityLifecycle(): Boolean
}

