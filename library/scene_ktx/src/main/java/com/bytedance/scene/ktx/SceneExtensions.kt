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
package com.bytedance.scene.ktx

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.os.Handler
import android.os.Looper
import android.support.v4.app.FragmentActivity
import com.bytedance.scene.Scene

@Suppress("all")
private val HANDLER by lazy { Handler(Looper.getMainLooper()) }

fun Scene.post(runnable: Runnable) {
    if (this.lifecycle.currentState == Lifecycle.State.DESTROYED) {
        return
    }
    HANDLER.post(runnable)
    this.lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            HANDLER.removeCallbacks(runnable)
        }
    })
}

fun Scene.postDelayed(runnable: Runnable, delayMillis: Long) {
    if (this.lifecycle.currentState == Lifecycle.State.DESTROYED) {
        return
    }
    HANDLER.postDelayed(runnable, delayMillis)
    this.lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            HANDLER.removeCallbacks(runnable)
        }
    })
}

fun Scene.isDestroyed(): Boolean {
    return this.lifecycle.currentState == Lifecycle.State.DESTROYED
}

fun Scene.fragmentActivity(): FragmentActivity? {
    return activity as FragmentActivity?
}

fun Scene.requireFragmentActivity(): FragmentActivity {
    return requireActivity() as FragmentActivity
}