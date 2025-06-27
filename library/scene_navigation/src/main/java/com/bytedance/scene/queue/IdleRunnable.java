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
package com.bytedance.scene.queue;

/**
 * Created by jiangqi on 2024/11/25
 *
 * @author jiangqi@bytedance.com
 */

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;

import androidx.annotation.RestrictTo;

/**
 * @hide
 */
@RestrictTo(LIBRARY)
class IdleRunnable implements MessageQueue.IdleHandler, Runnable {
    private final Handler mHandler;
    private final long timeOutMillis;
    private final Runnable mTargetRunnable;

    private boolean isAddedToIdle = false;
    private boolean isAddedToQueue = false;

    IdleRunnable(Handler handler, Runnable targetRunnable, long timeOutMillis) {
        this.mHandler = handler;
        this.mTargetRunnable = targetRunnable;
        if (timeOutMillis <= 0) {
            throw new IllegalArgumentException("timeOutMillis should >0");
        }
        this.timeOutMillis = timeOutMillis;
    }

    void start() {
        this.isAddedToIdle = true;
        this.isAddedToQueue = true;

        Looper.myQueue().addIdleHandler(this);
        this.mHandler.postDelayed(this, timeOutMillis);
    }

    @Override
    public boolean queueIdle() {
        this.isAddedToIdle = false;
        this.mHandler.removeCallbacks(this);
        this.isAddedToQueue = false;
        this.runInternal();
        return false;
    }

    @Override
    public void run() {
        this.isAddedToQueue = false;
        Looper.myQueue().removeIdleHandler(this);
        this.isAddedToIdle = false;
        this.runInternal();
    }

    private void runInternal() {
        this.mTargetRunnable.run();
    }

    void cancel() {
        if (this.isAddedToQueue) {
            this.isAddedToQueue = false;
            this.mHandler.removeCallbacks(this);
        }
        if (this.isAddedToIdle) {
            this.isAddedToIdle = false;
            Looper.myQueue().removeIdleHandler(this);
        }
    }

    boolean hasStarted() {
        return this.isAddedToQueue || this.isAddedToIdle;
    }

    void forceExecute() {
        this.cancel();
        this.runInternal();
    }
}
