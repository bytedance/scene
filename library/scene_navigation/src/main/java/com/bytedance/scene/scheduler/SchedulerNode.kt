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

/**
 * Created by jiangqi on 2025/4/25
 * @author jiangqi@bytedance.com
 */


typealias SIGNAL = () -> Unit
typealias StartAction = () -> Unit
typealias EndAction = () -> Unit


class SchedulerNode(
    private val sharedEnvironment: SharedEnvironment,
    private val navigationTaskExecutor: INavigationTaskExecutor,
    private val messageQueue: IMessageQueue,
    private val schedulerNodePolicy: SchedulerNodePolicy,
    private val terminationSignal: IForcibleTerminationSignal,
    private val operation: ForcibleTerminationOperation
) {
    private var nextSchedulerNode: SchedulerNode? = null
    private var isNodeCompleted = false

    init {
        this.terminationSignal.addTerminationCallback {
            if (isNodeCompleted) {
                this.nextMessage?.let {
                    messageQueue.removeMessage(it)
                    scheduleNextTask(SchedulerNodePolicy.NEXT_IMMEDIATE)
                }
            }
        }
    }

    fun postNextAtFront(nextOperation: ForcibleTerminationOperation): SchedulerNode {
        return nextLoop(nextOperation, SchedulerNodePolicy.NEXT_LOOP_HEAD)
    }

    fun postNext(nextOperation: ForcibleTerminationOperation): SchedulerNode {
        return nextLoop(nextOperation, SchedulerNodePolicy.NEXT_LOOP_TAIL)
    }

    fun runNext(nextOperation: ForcibleTerminationOperation): SchedulerNode {
        return nextLoop(nextOperation, SchedulerNodePolicy.NEXT_IMMEDIATE)
    }

    private fun nextLoop(
        nextOperation: ForcibleTerminationOperation, nextMode: SchedulerNodePolicy
    ): SchedulerNode {
        if (this.nextSchedulerNode != null) {
            throw IllegalArgumentException("Duplicate next")
        }

        val childTerminationSignal = ForcibleTerminationSignal()

        val newSchedulerNode = SchedulerNode(
            sharedEnvironment,
            navigationTaskExecutor,
            messageQueue,
            nextMode,
            childTerminationSignal,
            nextOperation
        )

        terminationSignal.addTerminationCallback {
            childTerminationSignal.forceFinish()
        }
        this.nextSchedulerNode = newSchedulerNode
        return newSchedulerNode
    }

    private var nextMessage: Runnable? = null

    private fun scheduleNextTask(overrideMode: SchedulerNodePolicy?) {
        val nextScheduler = this.nextSchedulerNode
        if (nextScheduler == null) {
            doFinish()
            return
        }

        when (nextScheduler.schedulerNodePolicy) {
            SchedulerNodePolicy.NEXT_IMMEDIATE -> {
                nextScheduler.run()
            }

            SchedulerNodePolicy.NEXT_LOOP_HEAD -> {
                this.nextMessage = messageQueue.postMessageAtHead { nextScheduler.run() }
            }

            SchedulerNodePolicy.NEXT_LOOP_TAIL -> {
                this.nextMessage = messageQueue.postMessage { nextScheduler.run() }
            }
        }
    }

    internal fun run() {
        this.operation._terminationSignal = terminationSignal
        navigationTaskExecutor.execute(this.operation) {
            doNext()
        }
    }

    private fun doNext() {
        this.isNodeCompleted = true
        if (this.terminationSignal.isForceFinished) {
            scheduleNextTask(SchedulerNodePolicy.NEXT_IMMEDIATE)
        } else {
            scheduleNextTask(null)
        }
    }

    fun startAction(action: StartAction): SchedulerNode {
        return this
    }

    fun endAction(action: EndAction): SchedulerNode {
        this.sharedEnvironment.setEndAction(action)
        return this
    }

    fun endAction(action: Runnable): SchedulerNode {
        this.sharedEnvironment.setEndAction {
            action.run()
        }
        return this
    }

    fun start() {
        this.sharedEnvironment.start()
    }

    private fun doFinish() {
        this.sharedEnvironment.doEnd()
    }

    fun checkFinishStatus() {
        this.nextSchedulerNode?.checkFinishStatus()
        if (this.isNodeCompleted) {
            return
        } else {
            throw IllegalStateException("Node is already finished $operation")
        }
    }
}


