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
package com.bytedance.scene.navigation.scheduler

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.bytedance.scene.scheduler.IMessageQueue

internal class DefaultMessageQueue : IMessageQueue {
    private val handler = Handler(Looper.getMainLooper())

    override fun postMessage(runnable: Runnable): Runnable {
        val message: Message = Message.obtain(handler, runnable)
        handler.sendMessage(message)
        return runnable
    }

    override fun postMessageAtHead(runnable: Runnable): Runnable {
        val message: Message = Message.obtain(handler, runnable)
        handler.sendMessageAtFrontOfQueue(message)
        return runnable
    }

    override fun removeMessage(runnable: Runnable) {
        handler.removeCallbacks(runnable)
    }
}