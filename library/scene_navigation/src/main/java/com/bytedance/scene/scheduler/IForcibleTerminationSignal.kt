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

import com.bytedance.scene.utlity.CancellationSignal
import com.bytedance.scene.utlity.CancellationSignalList

interface IForcibleTerminationSignal {
    val isForceFinished: Boolean
    fun forceFinish()
    fun addTerminationCallback(callback: () -> Unit)
}

class ForcibleTerminationSignal : IForcibleTerminationSignal {
    private val cancellationSignal = CancellationSignalList()

    override val isForceFinished: Boolean
        get() = this.cancellationSignal.isCanceled

    override fun forceFinish() {
        this.cancellationSignal.cancel()
    }

    override fun addTerminationCallback(callback: () -> Unit) {
        this.cancellationSignal.add(CancellationSignal().apply {
            this.setOnCancelListener(callback)
        })
    }
}