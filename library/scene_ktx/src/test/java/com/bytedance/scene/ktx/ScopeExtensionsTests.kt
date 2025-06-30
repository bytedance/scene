package com.bytedance.scene.ktx

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bytedance.scene.Scene
import com.bytedance.scene.State
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.utlity.ViewIdGenerator
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ScopeExtensionsTests {
    class TestGroupScene : GroupScene() {
        val mId: Int = ViewIdGenerator.generateViewId()

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
            return FrameLayout(requireSceneContext())
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            view.id = mId
        }
    }

    class TestChildScene : Scene() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            return View(requireSceneContext())
        }
    }

    @Test
    fun test() {
        val groupScene = TestGroupScene()
        createFromSceneLifecycleManager(groupScene)

        groupScene.scope["test"] = "Value"
        val childScene = TestChildScene()
        groupScene.add(groupScene.mId, childScene, "Test")

        val value: String = childScene.scope["test"]
        Assert.assertEquals("Value", value)
        Assert.assertNull(childScene.scope["test_not_exists"])
    }

    @Test
    fun testScenePost() {
        val scene = TestChildScene()
        val sceneLifecycleManager = createFromInitSceneLifecycleManager(scene).first
        val observerCount = scene.lifecycleObserverCount
        var a = 0
        scene.post {
            a = 1
        }
        Assert.assertEquals(observerCount + 1, scene.lifecycleObserverCount)

        ShadowLooper.runUiThreadTasks()
        Assert.assertEquals(1, a)
        Assert.assertEquals(observerCount, scene.lifecycleObserverCount)

        scene.post {
            a = 2
        }
        Assert.assertEquals(observerCount + 1, scene.lifecycleObserverCount)

        sceneLifecycleManager.onDestroyView()
        ShadowLooper.runUiThreadTasks()
        Assert.assertEquals(1, a)
        Assert.assertEquals(observerCount, scene.lifecycleObserverCount)
    }
}