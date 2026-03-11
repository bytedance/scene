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
package com.bytedance.scene.navigation

import com.bytedance.scene.interfaces.PushResultCallback

/**
 * A -> B
 * A：caller
 * B: callee
 * A Scene can only be called as a CalleeHandler role, but it can call multiples other Scenes
 */
class SceneResultHandler(private val calleeHandler: CalleeHandler?) {

    var callerHandlerList: MutableList<CallerHandler> = arrayListOf()

    fun setCalleeResult(result: Any?) {
        this.calleeHandler?.invoke(result)
    }

    fun deliverResults() {
        this.callerHandlerList.forEach {
            it.deliverResult()
        }
        this.callerHandlerList.clear()
    }
}

class CallerHandler(private val pushResultCallback: PushResultCallback) {
    private var latestResult: Any? = null

    fun cacheResult(result: Any?) {
        this.latestResult = result
    }

    fun deliverResult() {
        //always will deliver a result
        this.pushResultCallback.onResult(this.latestResult)
    }
}

typealias CalleeHandler = (Any?) -> Unit