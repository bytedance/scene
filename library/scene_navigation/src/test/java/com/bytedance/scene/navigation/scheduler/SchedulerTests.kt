package com.bytedance.scene.navigation.scheduler

import com.bytedance.scene.scheduler.ForcibleTerminationOperation
import com.bytedance.scene.scheduler.buildScheduler
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SchedulerTests {
    @Test
    fun test() {
        val aOperation = AOperation()
        val bOperation = BOperation()
        buildScheduler(
            DefaultNavigationTaskExecutor(), DefaultMessageQueue()
        ).runNext(aOperation).runNext(bOperation).postNextAtFront(COperation())
            .postNext(DOperation()).startAction {

            }.endAction {

            }.start()
        Assert.assertTrue(aOperation.isExecuted)
    }

    class AOperation : ForcibleTerminationOperation() {
        var isExecuted = false
        override fun execute(endAction: Runnable) {
            this.isExecuted = true
            endAction.run()
        }
    }

    class BOperation : ForcibleTerminationOperation() {
        override fun execute(endAction: Runnable) {
            endAction.run()
        }
    }

    class COperation : ForcibleTerminationOperation() {
        override fun execute(endAction: Runnable) {
            endAction.run()
        }
    }

    class DOperation : ForcibleTerminationOperation() {
        override fun execute(endAction: Runnable) {
            endAction.run()
        }
    }
}