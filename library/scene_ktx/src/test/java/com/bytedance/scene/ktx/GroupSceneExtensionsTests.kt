package com.bytedance.scene.ktx

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bytedance.scene.Scene
import com.bytedance.scene.SceneComponentFactory
import com.bytedance.scene.SceneLifecycleManager
import com.bytedance.scene.Scope
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.navigation.NavigationScene
import com.bytedance.scene.navigation.NavigationSceneOptions
import com.bytedance.scene.utlity.ViewIdGenerator
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GroupSceneExtensionsTests {
    private fun createFromSceneLifecycleManager(rootScene: Scene): NavigationScene {
        val pair = createFromInitSceneLifecycleManager(rootScene)
        val sceneLifecycleManager = pair.first
        sceneLifecycleManager.onStart()
        sceneLifecycleManager.onResume()
        return pair.second
    }

    private fun createFromInitSceneLifecycleManager(rootScene: Scene): Pair<SceneLifecycleManager, NavigationScene> {
        val controller = Robolectric.buildActivity<TestActivity>(TestActivity::class.java!!).create().start().resume()
        val testActivity = controller.get()
        val navigationScene = NavigationScene()
        val options = NavigationSceneOptions(rootScene.javaClass)
        navigationScene.setArguments(options.toBundle())

        val navigationSceneHost = object : NavigationScene.NavigationSceneHost {
            override fun isSupportRestore(): Boolean {
                return false
            }

            override fun startActivityForResult(intent: Intent, requestCode: Int) {

            }

            override fun requestPermissions(permissions: Array<String>, requestCode: Int) {

            }
        }

        val rootScopeFactory = Scope.RootScopeFactory { Scope.DEFAULT_ROOT_SCOPE_FACTORY.rootScope }

        val sceneComponentFactory = SceneComponentFactory { cl, className, bundle ->
            if (className == rootScene.javaClass.name) {
                rootScene
            } else null
        }

        navigationScene.defaultNavigationAnimationExecutor = NoAnimationExecutor()

        val sceneLifecycleManager = SceneLifecycleManager()
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, navigationSceneHost, rootScopeFactory,
                sceneComponentFactory, null)
        return Pair(sceneLifecycleManager, navigationScene)
    }

    class TestActivity : Activity() {
        lateinit var mFrameLayout: FrameLayout

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            mFrameLayout = FrameLayout(this)
            setContentView(mFrameLayout)
        }
    }

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