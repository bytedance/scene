package com.bytedance.scene.navigation.configuration

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.Scene
import com.bytedance.scene.SceneLifecycleManager
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior
import com.bytedance.scene.interfaces.PopOptions
import com.bytedance.scene.navigation.ActivityCompatibleInfoCollector
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * Created by jiangqi on 2024/11/5
 * @author jiangqi@bytedance.com
 *
 * Only dispatch top scene onConfigurationChanged callback, and dispatch to previous Scene when it is visible
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ConfigurationChangedTopSceneTests {
    /**
     * scene1 onConfigurationChanged()
     * scene2 onConfigurationChanged()
     * scene3 onConfigurationChanged()
     */
    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    fun `test receive onConfigurationChanged when scene is visible`() {
        val scene1 = TestOnConfigurationChangedCallbackScene()
        val pair = createFromInitSceneLifecycleManager(scene1, true, false)

        val sceneLifecycleManager: SceneLifecycleManager<*> = pair!!.first
        val navigationScene = pair.second

        val scene2 = TestOnConfigurationChangedCallbackScene()
        navigationScene.push(scene2)

        val scene3 = TestOnConfigurationChangedCallbackScene()
        navigationScene.push(scene3)

        Assert.assertEquals(0, scene1.onConfigurationChangedCount)
        Assert.assertEquals(0, scene2.onConfigurationChangedCount)
        Assert.assertEquals(0, scene3.onConfigurationChangedCount)

        navigationScene.onConfigurationChanged(createConfigurationForOrientation(false))
        Assert.assertSame(navigationScene.currentScene, scene3)

        Assert.assertEquals(0, scene1.onConfigurationChangedCount)
        Assert.assertEquals(0, scene2.onConfigurationChangedCount)
        Assert.assertEquals(1, scene3.onConfigurationChangedCount)

        navigationScene.pop()
        Assert.assertSame(navigationScene.currentScene, scene2)

        Assert.assertEquals(0, scene1.onConfigurationChangedCount)
        Assert.assertEquals(1, scene2.onConfigurationChangedCount)

        navigationScene.pop(PopOptions.Builder().setUsePost(true).build())

        Assert.assertEquals(0, scene1.onConfigurationChangedCount)

        Shadows.shadowOf(Looper.getMainLooper()).idle() //execute Handler posted task

        Assert.assertSame(navigationScene.currentScene, scene1)

        Assert.assertEquals(1, scene1.onConfigurationChangedCount)
    }

    /**
     * scene1 recreate
     * scene2 recreate
     * scene3 recreate
     */
    @Test
    fun `test recreate when scene is visible`() {
        val scene1 = TestOnConfigurationChangedRecreateScene()
        val pair = createFromInitSceneLifecycleManager(scene1, true, false)

        val sceneLifecycleManager: SceneLifecycleManager<*> = pair!!.first
        val navigationScene = pair.second

        val scene2 = TestOnConfigurationChangedRecreateScene()
        navigationScene.push(scene2)

        val scene3 = TestOnConfigurationChangedRecreateScene()
        navigationScene.push(scene3)

        Assert.assertEquals(0, scene1.onConfigurationChangedCount)
        Assert.assertEquals(0, scene2.onConfigurationChangedCount)
        Assert.assertEquals(0, scene3.onConfigurationChangedCount)

        navigationScene.onConfigurationChanged(createConfigurationForOrientation(false))

        Assert.assertEquals(0, scene1.onConfigurationChangedCount)
        Assert.assertEquals(0, scene2.onConfigurationChangedCount)
        Assert.assertEquals(0, scene3.onConfigurationChangedCount)

        Assert.assertNotSame(navigationScene.currentScene, scene3)

        navigationScene.pop()

        Assert.assertNotSame(navigationScene.currentScene, scene2)

        Assert.assertEquals(0, scene1.onConfigurationChangedCount)
        Assert.assertEquals(0, scene2.onConfigurationChangedCount)

        navigationScene.pop(PopOptions.Builder().setUsePost(true).build())

        Assert.assertEquals(0, scene1.onConfigurationChangedCount)

        Shadows.shadowOf(Looper.getMainLooper()).idle() //execute Handler posted task

        // root scene is recreated by rootSceneComponentFactory
        Assert.assertSame(navigationScene.currentScene, scene1)

        Assert.assertEquals(0, scene1.onConfigurationChangedCount)
    }

    /**
     * scene1 onConfigurationChanged()
     * scene2 recreate
     * scene3 onConfigurationChanged()
     * scene4 recreate
     */
    @Test
    fun `test recreate or receive onConfigurationChanged when scene is visible`() {
        //will receive callback
        val scene1 = TestOnConfigurationChangedCallbackScene()
        val pair = createFromInitSceneLifecycleManager(scene1, true, false)

        val sceneLifecycleManager: SceneLifecycleManager<*> = pair!!.first
        val navigationScene = pair.second

        //will recreate
        val scene2 = TestOnConfigurationChangedRecreateScene()
        navigationScene.push(scene2)

        //will receive callback
        val scene3 = TestOnConfigurationChangedCallbackScene()
        navigationScene.push(scene3)

        //will recreate
        val scene4 = TestOnConfigurationChangedRecreateScene()
        navigationScene.push(scene4)

        Assert.assertEquals(0, scene1.onConfigurationChangedCount)
        Assert.assertEquals(0, scene2.onConfigurationChangedCount)
        Assert.assertEquals(0, scene3.onConfigurationChangedCount)
        Assert.assertEquals(0, scene4.onConfigurationChangedCount)

        navigationScene.onConfigurationChanged(createConfigurationForOrientation(false))

        Assert.assertEquals(0, scene1.onConfigurationChangedCount)
        Assert.assertEquals(0, scene2.onConfigurationChangedCount)
        Assert.assertEquals(0, scene3.onConfigurationChangedCount)
        Assert.assertEquals(0, scene4.onConfigurationChangedCount)

        Assert.assertNotSame(navigationScene.currentScene, scene4)

        navigationScene.pop()

        Assert.assertSame(navigationScene.currentScene, scene3)

        Assert.assertEquals(0, scene1.onConfigurationChangedCount)
        Assert.assertEquals(0, scene2.onConfigurationChangedCount)
        Assert.assertEquals(1, scene3.onConfigurationChangedCount)

        navigationScene.pop(PopOptions.Builder().setUsePost(true).build())

        Assert.assertEquals(0, scene1.onConfigurationChangedCount)
        Assert.assertEquals(0, scene2.onConfigurationChangedCount)

        Shadows.shadowOf(Looper.getMainLooper()).idle() //execute Handler posted task

        Assert.assertNotSame(navigationScene.currentScene, scene2)

        navigationScene.pop()

        Assert.assertSame(navigationScene.currentScene, scene1)
        Assert.assertEquals(1, scene1.onConfigurationChangedCount)
    }
}

//will receive onConfigurationChanged when CONFIG_ORIENTATION or CONFIG_UI_MODE changed
class TestOnConfigurationChangedCallbackScene : Scene(), ActivityCompatibleBehavior {
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

//will recreated when CONFIG_ORIENTATION changed
class TestOnConfigurationChangedRecreateScene : Scene(), ActivityCompatibleBehavior {
    var onConfigurationChangedCount = 0

    init {
        val data = ActivityCompatibleInfoCollector.getOrCreateHolder(this)
        if (data.configChanges != null) {
            throw IllegalArgumentException("activityAttributes can't invoke more than once")
        }
        data.configChanges = ActivityInfo.CONFIG_UI_MODE
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