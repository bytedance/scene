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
package com.bytedance.scene.navigation;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Priority to ensure that the life cycle is correct,
 * and then ensure that the animation is coherent
 * Push/Pop requires NavigationScene to be onResume state.
 * If not, subsequent the execution.
 * The subsequent Scene life cycle will wait for the next UI loop.
 */
class AsyncHandler extends Handler {
    private boolean async = true;

    @SuppressLint("NewApi")
    AsyncHandler(Looper looper) {
        super(looper);
        if (Build.VERSION.SDK_INT < 16) {
            async = false;
        } else if (async && Build.VERSION.SDK_INT < 22) {
            // Confirm that the method is available on this API level despite being @hide.
            Message message = Message.obtain();
            try {
                message.setAsynchronous(true);
            } catch (NoSuchMethodError e) {
                async = false;
            }
            message.recycle();
        }
    }

    @SuppressLint("NewApi")
    public void postAsyncIfNeeded(Runnable runnable) {
        Message message = Message.obtain(this, runnable);
        if (async) {
            message.setAsynchronous(true);
        }
        sendMessage(message);
    }
}