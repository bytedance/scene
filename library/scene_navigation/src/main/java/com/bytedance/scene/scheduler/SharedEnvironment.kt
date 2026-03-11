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
package com.bytedance.scene.scheduler

class SharedEnvironment() {
    private var isStarted = false
    private var isAborted = false
    private var startSignal: SIGNAL? = null
    private var rootNode: SchedulerNode? = null
    private var endAction: EndAction? = null
    val terminationSignal: IForcibleTerminationSignal = ForcibleTerminationSignal()

    fun setStartSignal(signal: SIGNAL) {
        this.startSignal = signal
    }

    fun setEndAction(action: EndAction) {
        if (this.endAction != null) {
            throw IllegalArgumentException("already set")
        }
        this.endAction = action
    }

    fun doEnd() {
        this.endAction?.invoke()
    }

    fun start() {
        if (this.isStarted) {
            throw IllegalArgumentException("Task is already started")
        }
        this.isStarted = true
        startSignal?.invoke()
    }

    fun isForceFinished(): Boolean {
        return this.isAborted
    }

    fun forceFinish() {
        if (this.isAborted) {
            throw IllegalArgumentException("Task is already aborted")
        }
        this.isAborted = true
        this.terminationSignal.forceFinish()
        this.rootNode?.checkFinishStatus()
    }
}