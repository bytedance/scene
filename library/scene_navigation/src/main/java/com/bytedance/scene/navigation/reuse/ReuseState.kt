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

/**
 * Reuse states for IReuseScene
 *
 * Created by zhuqingying on 2025/2/12
 * @author zhuqingying@bytedance.com
 */
enum class ReuseState {
    /**
     * Initial state
     */
    INITED,

    /**
     * Currently being reused
     */
    REUSED,

    /**
     * Released to reuse pool
     */
    RELEASED,
}