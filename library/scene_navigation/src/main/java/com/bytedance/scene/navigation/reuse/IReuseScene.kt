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

import android.os.Bundle
import com.bytedance.scene.Scene
import com.bytedance.scene.navigation.NavigationSceneGetter

/**
 * Interface for reusable scenes
 *
 * Created by zhuqingying on 2025/2/8
 * @author zhuqingying@bytedance.com
 */
interface IReuseScene {
    /**
     * Determines if this scene supports reuse
     */
    fun isReusable(): Boolean

    /**
     * Called when the scene is being reused from the pool
     * Business logic can perform compensation operations here
     */
    fun onPrepare(bundle: Bundle?)

    /**
     * Called when the scene is released to the pool
     * Business logic can perform reset operations here
     */
    fun onRelease()
}

/**
 * Extension function to check if a Scene is released
 * Replaces isDestroyed || isFinishing
 * Returns true if the Scene has triggered onRelease or has been destroyed
 */
fun Scene.isReleased(): Boolean {
    if (this !is IReuseScene) {
        return this.isViewDestroyed
    }
    if (this.isViewDestroyed) {
        return true
    }
    val navigationScene = NavigationSceneGetter.getNavigationScene(this) ?: return false
    return navigationScene.isReleased(this)
}
