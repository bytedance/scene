package com.bytedance.scene.navigation.post.queue

import com.bytedance.scene.utlity.TaskStartSignal
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TaskStartSignalTests {
    @Test
    fun testStart() {
        var value = false
        val signal = TaskStartSignal()
        signal.setRunnable {
            value = true
        }
        Assert.assertFalse(value)
        signal.start()
        Assert.assertTrue(signal.isStarted)
        Assert.assertTrue(value)
    }
}