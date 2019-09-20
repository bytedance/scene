package com.bytedance.scene.ktx

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bytedance.scene.Scene
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.utlity.ViewIdGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GroupSceneExtensionsTests {
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

    class TestChildScene2 : Scene() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            return View(requireSceneContext())
        }
    }

    @Test
    fun testShow() {
        val groupScene = TestGroupScene()
        createFromSceneLifecycleManager(groupScene)

        val childScene = TestChildScene()
        groupScene.add(groupScene.mId, childScene, "Test")
        assertEquals(childScene.state, com.bytedance.scene.State.RESUMED)
        groupScene.hide(childScene)
        assertEquals(childScene.state, com.bytedance.scene.State.ACTIVITY_CREATED)
        groupScene.show("Test")
        assertEquals(childScene.state, com.bytedance.scene.State.RESUMED)
    }

    @Test
    fun testHide() {
        val groupScene = TestGroupScene()
        createFromSceneLifecycleManager(groupScene)

        val childScene = TestChildScene()
        groupScene.add(groupScene.mId, childScene, "Test")
        assertEquals(childScene.state, com.bytedance.scene.State.RESUMED)
        groupScene.hide("Test")
        assertEquals(childScene.state, com.bytedance.scene.State.ACTIVITY_CREATED)
    }

    @Test
    fun testRemove() {
        val groupScene = TestGroupScene()
        createFromSceneLifecycleManager(groupScene)

        val childScene = TestChildScene()
        groupScene.add(groupScene.mId, childScene, "Test")
        assertEquals(childScene.state, com.bytedance.scene.State.RESUMED)
        groupScene.remove("Test")
        assertEquals(childScene.state, com.bytedance.scene.State.NONE)
    }

    @Test
    fun testAddAndHide() {
        val groupScene = TestGroupScene()
        createFromSceneLifecycleManager(groupScene)

        val childScene = TestChildScene()
        groupScene.addAndHide(groupScene.mId, childScene, "Test")
        assertFalse(childScene.stateHistory.contains(com.bytedance.scene.State.RESUMED.getName()))
        assertEquals(childScene.state, com.bytedance.scene.State.ACTIVITY_CREATED)
    }

    @Test
    fun testReplace0() {
        val groupScene = TestGroupScene()
        createFromSceneLifecycleManager(groupScene)

        val childScene = TestChildScene()
        val childScene2 = TestChildScene2()

        groupScene.add(groupScene.mId, childScene, "Test")
        assertEquals(childScene.state, com.bytedance.scene.State.RESUMED)
        assertEquals(childScene2.state, com.bytedance.scene.State.NONE)

        groupScene.replace(groupScene.mId, childScene2, "Test")
        assertEquals(childScene.state, com.bytedance.scene.State.NONE)
        assertEquals(childScene2.state, com.bytedance.scene.State.RESUMED)
    }

    @Test
    fun testReplace1() {
        val groupScene = TestGroupScene()
        createFromSceneLifecycleManager(groupScene)

        val childScene = TestChildScene()

        groupScene.add(groupScene.mId, childScene, "Test")
        assertEquals(childScene.state, com.bytedance.scene.State.RESUMED)

        groupScene.replace(groupScene.mId, childScene, "Test")
        assertEquals(childScene.state, com.bytedance.scene.State.RESUMED)
    }

    @Test
    fun testReplace2() {
        val groupScene = TestGroupScene()
        createFromSceneLifecycleManager(groupScene)

        val childScene = TestChildScene()
        val childScene2 = TestChildScene2()

        groupScene.add(groupScene.mId, childScene, "Test")
        groupScene.add(groupScene.mId, childScene2, "Test1")
        assertEquals(childScene.state, com.bytedance.scene.State.RESUMED)
        assertEquals(childScene2.state, com.bytedance.scene.State.RESUMED)

        groupScene.replace(groupScene.mId, childScene2, "Test")
        assertEquals(childScene.state, com.bytedance.scene.State.NONE)
        assertEquals(childScene2.state, com.bytedance.scene.State.RESUMED)
    }

    @Test
    fun testReplace3() {
        val groupScene = TestGroupScene()
        createFromSceneLifecycleManager(groupScene)

        val childScene2 = TestChildScene2()

        groupScene.replace(groupScene.mId, childScene2, "Test1")
        assertEquals(childScene2.state, com.bytedance.scene.State.RESUMED)
    }

    @Test
    fun testReplace4() {
        val groupScene = TestGroupScene()
        createFromSceneLifecycleManager(groupScene)

        val childScene = TestChildScene()
        val childScene2 = TestChildScene2()

        groupScene.add(groupScene.mId, childScene, "Test")
        assertEquals(childScene.state, com.bytedance.scene.State.RESUMED)
        assertEquals(childScene2.state, com.bytedance.scene.State.NONE)

        groupScene.hide(childScene)
        assertEquals(childScene.state, com.bytedance.scene.State.ACTIVITY_CREATED)

        groupScene.replace(groupScene.mId, childScene2, "Test")
        assertEquals(childScene.state, com.bytedance.scene.State.NONE)
        assertEquals(childScene2.state, com.bytedance.scene.State.ACTIVITY_CREATED)
    }
}