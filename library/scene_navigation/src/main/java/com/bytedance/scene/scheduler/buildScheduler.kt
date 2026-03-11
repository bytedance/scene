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

import com.bytedance.scene.navigation.NavigationManagerAbility
import com.bytedance.scene.queue.NavigationMessageQueue
import com.bytedance.scene.queue.NavigationRunnable

fun buildScheduler(
    navigationTaskExecutor: INavigationTaskExecutor, navigationMessageQueue: IMessageQueue
): SchedulerBridge {
    return SchedulerBridge(SharedEnvironment(), navigationTaskExecutor, navigationMessageQueue)
}

fun buildScheduler(
    navigationManagerAbility: NavigationManagerAbility,
    navigationMessageQueue: NavigationMessageQueue
): SchedulerBridge {
    return SchedulerBridge(
        SharedEnvironment(), NavigationTaskExecutorProxy(navigationManagerAbility), MessageQueueProxy(navigationMessageQueue)
    )
}

class NavigationTaskExecutorProxy(private val navigationManagerAbility: NavigationManagerAbility) :
    INavigationTaskExecutor {
    override fun execute(
        operation: ForcibleTerminationOperation, endAction: EndAction
    ) {
        navigationManagerAbility.executeOperationSafely(operation, endAction)
    }
}

class MessageQueueProxy(private val navigationMessageQueue: NavigationMessageQueue) : IMessageQueue {
    private val map: MutableMap<Runnable, NavigationRunnable> = hashMapOf()
    override fun postMessage(runnable: Runnable): Runnable {
        val navigationRunnable = object : NavigationRunnable() {
            override fun run() {
                runnable.run()
            }
        }
        navigationMessageQueue.postAsync(navigationRunnable)
        return runnable
    }

    override fun postMessageAtHead(runnable: Runnable): Runnable {
        val navigationRunnable = object : NavigationRunnable() {
            override fun run() {
                runnable.run()
            }
        }
        navigationMessageQueue.postAsyncAtHead(navigationRunnable)
        return runnable
    }

    override fun removeMessage(runnable: Runnable) {
        map.remove(runnable)?.let {
            navigationMessageQueue.remove(it)
        }
    }
}