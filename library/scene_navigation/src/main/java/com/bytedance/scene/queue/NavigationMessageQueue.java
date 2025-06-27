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

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RestrictTo;

import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.utlity.SceneInternalException;
import com.bytedance.scene.utlity.ThreadUtility;

import java.util.LinkedList;

/**
 * Created by jiangqi on 2023/11/3
 *
 * @author jiangqi@bytedance.com
 * <p>
 * 一个是单次导航操作的内部调度
 * 另外一个是多次导航操作
 * <p>
 * 我只能保证导航的顺序，一定是之前的结束才继续下一个
 * <p>
 * BadCase，postAsync 的操作可能需要等到之前的 Operation 结束后才能执行，会被一定的延后
 *
 *
 * 单元测试，保证之前 postAsync 的任务都在 postSync 之前执行完，包括 postAsync 内部结束后又 postAsyncAtHead 的任务
 *
 * 其实 postAsyncAtHead 还是有点危险的
 */

/**
 * @hide
 */
@RestrictTo(LIBRARY)
public class NavigationMessageQueue {
    private static final String TAG = "NavigationMessageQueue";
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final LinkedList<NavigationRunnable> mPendingTask = new LinkedList<>();
    private boolean mIsRunningPostSync = false;
    private IdleRunnable mIdleRunnable = null;

    public void postAsync(final NavigationRunnable runnable) {
        ThreadUtility.checkUIThread();
        this.forceExecuteIdleTask();
        this.mPendingTask.add(runnable);
        this.mHandler.post(this.mSceneNavigationTask);
    }

    public void postAsyncAtHead(final NavigationRunnable runnable) {
        this.forceExecuteIdleTask();
        this.mPendingTask.add(0, runnable);
        this.mHandler.post(this.mSceneNavigationTask);
    }

    private void forceExecuteIdleTask(){
        if (this.mIdleRunnable != null && this.mIdleRunnable.hasStarted()) {
            this.mIdleRunnable.forceExecute();
            this.mIdleRunnable = null;
        }
    }

    public void executeWhenIdleOrTimeLimit(final NavigationRunnable runnable, long timeOutMillis) {
        this.forceExecuteIdleTask();

        if (this.mIdleRunnable != null) {
            throw new SceneInternalException("mIdleRunnable should be null");
        }

        this.mPendingTask.add(0, runnable);
        this.mIdleRunnable = new IdleRunnable(this.mHandler, this.mSceneNavigationTask, timeOutMillis);
        this.mIdleRunnable.start();
    }

    private final SceneMainRunnable mSceneNavigationTask = new SceneMainRunnable() {
        @Override
        public void run() {
            NavigationRunnable currentTask = NavigationMessageQueue.this.mPendingTask.poll();
            if (currentTask == null) {
                LoggerManager.getInstance().i(TAG, "empty return");
                return;
            }

            LoggerManager.getInstance().i(TAG, "run loop task " + currentTask.toString());
            currentTask.run();
        }
    };

    public void postSync(Runnable runnable) {
        ThreadUtility.checkUIThread();

        if (this.mIsRunningPostSync) {
            throw new SceneInternalException("Can't postSync");
        }

        this.mIsRunningPostSync = true;

        try {
            //execute all previous navigation tasks
            while (true) {
                NavigationRunnable currentTask = this.mPendingTask.poll();
                if (currentTask == null) {
                    break;
                }

                LoggerManager.getInstance().i(TAG, "postSync run loop previous task " + currentTask.toString());
                currentTask.run();
            }

            this.mHandler.removeCallbacks(this.mSceneNavigationTask);
            if (this.mIdleRunnable != null) {
                this.mIdleRunnable.cancel();
                this.mIdleRunnable = null;
            }

            //then execute this task
            LoggerManager.getInstance().i(TAG, "run loop current task " + runnable.toString());
            runnable.run();
        } finally {
            this.mIsRunningPostSync = false;
        }
    }

    public boolean hasPendingTasks() {
        return mPendingTask.size() > 0;
    }
}
