package com.bytedance.scene.navigation.configuration

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.util.Pair
import com.bytedance.scene.Scene
import com.bytedance.scene.SceneComponentFactory
import com.bytedance.scene.SceneLifecycleManager
import com.bytedance.scene.Scope
import com.bytedance.scene.Scope.RootScopeFactory
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior
import com.bytedance.scene.navigation.ActivityCompatibleInfoCollector
import com.bytedance.scene.navigation.NavigationScene
import com.bytedance.scene.navigation.NavigationSceneOptions
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Created by jiangqi on 2024/11/5
 * @author jiangqi@bytedance.com
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ConfigurationChangedTests {
    @Test
    fun `test Orientation onConfigurationChanged`() {
        val scene = TestOnConfigurationChangedScene()
        val pair = createFromInitSceneLifecycleManager(scene, true, false)

        val sceneLifecycleManager: SceneLifecycleManager<*> = pair!!.first
        val navigationScene = pair.second

        Assert.assertEquals(0, scene.onConfigurationChangedCount)

        navigationScene.onConfigurationChanged(createConfigurationForOrientation(false))

        Assert.assertEquals(1, scene.onConfigurationChangedCount)

        navigationScene.onConfigurationChanged(createConfigurationForOrientation(false))

        Assert.assertEquals(1, scene.onConfigurationChangedCount)

        navigationScene.onConfigurationChanged(createConfigurationForOrientation(true))
        Assert.assertEquals(2, scene.onConfigurationChangedCount)
    }

    @Test
    fun `test Night onConfigurationChanged`() {
        val scene = TestOnConfigurationChangedScene()
        val pair = createFromInitSceneLifecycleManager(scene, true, true)

        val sceneLifecycleManager: SceneLifecycleManager<*> = pair!!.first
        val navigationScene = pair.second

        Assert.assertEquals(0, scene.onConfigurationChangedCount)

        navigationScene.onConfigurationChanged(createConfigurationForDayNight(true))

        Assert.assertEquals(1, scene.onConfigurationChangedCount)

        navigationScene.onConfigurationChanged(createConfigurationForDayNight(true))

        Assert.assertEquals(1, scene.onConfigurationChangedCount)

        navigationScene.onConfigurationChanged(createConfigurationForDayNight(false))
        Assert.assertEquals(2, scene.onConfigurationChangedCount)
    }

    @Test
    fun `test normal ActivityCompatibleBehavior, should not receive callback or recreate`() {
        val scene = TestNormalActivityCompatibleBehaviorScene()
        val pair = createFromInitSceneLifecycleManager(scene, true, false)

        val sceneLifecycleManager: SceneLifecycleManager<*> = pair!!.first
        val navigationScene = pair.second

        Assert.assertEquals(0, scene.onConfigurationChangedCount)

        navigationScene.onConfigurationChanged(createConfigurationForDayNight(true))

        Assert.assertEquals(0, scene.onConfigurationChangedCount)
        Assert.assertSame(scene, navigationScene.currentScene)

        navigationScene.onConfigurationChanged(createConfigurationForDayNight(true))

        Assert.assertEquals(0, scene.onConfigurationChangedCount)
        Assert.assertSame(scene, navigationScene.currentScene)

        navigationScene.onConfigurationChanged(createConfigurationForDayNight(false))
        Assert.assertEquals(0, scene.onConfigurationChangedCount)
        Assert.assertSame(scene, navigationScene.currentScene)
    }
}

interface IActivity {
    var mFrameLayout: FrameLayout?
}

class TestConfigurationChangedActivity : Activity(), IActivity {
    override var mFrameLayout: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        applyOverrideConfiguration(createConfigurationForOrientation(true))
        super.onCreate(savedInstanceState)
        mFrameLayout = FrameLayout(this)
        setContentView(mFrameLayout)
    }
}

class TestNightConfigurationChangedActivity : Activity(), IActivity {
    override var mFrameLayout: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        applyOverrideConfiguration(createConfigurationForDayNight(false))
        super.onCreate(savedInstanceState)
        mFrameLayout = FrameLayout(this)
        setContentView(mFrameLayout)
    }
}

fun createFromInitSceneLifecycleManager(
    rootScene: Scene, separateCreate: Boolean, night: Boolean
): Pair<SceneLifecycleManager<NavigationScene>, NavigationScene>? {
    val controller = Robolectric.buildActivity(
        if (night) {
            TestNightConfigurationChangedActivity::class.java
        } else {
            TestConfigurationChangedActivity::class.java
        }
    ).create().start().resume()
    val testActivity = controller.get()
    val navigationScene = NavigationScene()
    val options = NavigationSceneOptions(rootScene.javaClass)
    options.setOnlyRestoreVisibleScene(true)
    navigationScene.isSeparateCreateFromCreateView = separateCreate
    navigationScene.setInitRootSceneOnCreate(separateCreate)
    navigationScene.setArguments(options.toBundle())
    val rootScopeFactory = RootScopeFactory { Scope.DEFAULT_ROOT_SCOPE_FACTORY.rootScope }
    val sceneComponentFactory = SceneComponentFactory { cl, className, bundle ->
        if (className == rootScene.javaClass.name) {
            rootScene
        } else null
    }
    navigationScene.defaultNavigationAnimationExecutor = NoAnimationExecutor()
    navigationScene.setRootSceneComponentFactory(sceneComponentFactory)
    val sceneLifecycleManager = SceneLifecycleManager<NavigationScene>()
    sceneLifecycleManager.onActivityCreated(
        testActivity, testActivity.mFrameLayout!!, navigationScene, rootScopeFactory, false, null
    )
    return Pair.create(sceneLifecycleManager, navigationScene)
}

class TestOnConfigurationChangedScene : Scene(), ActivityCompatibleBehavior {
    var onConfigurationChangedCount = 0

    init {
        val data = ActivityCompatibleInfoCollector.getOrCreateHolder(this)
        if (data.configChanges != null) {
            throw IllegalArgumentException("activityAttributes can't invoke more than once")
        }
        data.configChanges = ActivityInfo.CONFIG_ORIENTATION or ActivityInfo.CONFIG_UI_MODE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?
    ): View {
        return View(requireSceneContext())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        onConfigurationChangedCount++
    }

    override fun onNewIntent(arguments: Bundle?) {

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {

    }
}

class TestNormalActivityCompatibleBehaviorScene : Scene(), ActivityCompatibleBehavior {
    var onConfigurationChangedCount = 0

    init {
        val data = ActivityCompatibleInfoCollector.getOrCreateHolder(this)
        if (data.configChanges != null) {
            throw IllegalArgumentException("activityAttributes can't invoke more than once")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?
    ): View {
        return View(requireSceneContext())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        onConfigurationChangedCount++
    }

    override fun onNewIntent(arguments: Bundle?) {

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {

    }
}