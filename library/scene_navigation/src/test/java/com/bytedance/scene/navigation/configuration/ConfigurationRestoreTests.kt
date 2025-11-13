package com.bytedance.scene.navigation.configuration

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bytedance.scene.Scene
import com.bytedance.scene.SceneLifecycleManager
import com.bytedance.scene.Scope
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
 * Created by jiangqi on 2024/11/6
 * @author jiangqi@bytedance.com
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ConfigurationRestoreTests {
    /**
     * default Configuration.ORIENTATION_PORTRAIT
     * scene receive Configuration.ORIENTATION_LANDSCAPE
     * after save and restore, scene has default Configuration.ORIENTATION_PORTRAIT
     */
    @Test
    fun testSceneConfigurationRecreateWhenItsViewIsCreated() {
        val bundle = Bundle();
        val newConfig = createConfigurationForOrientation(false)

        if (true) {
            val controller =
                Robolectric.buildActivity(TestConfigurationChangedRestoreActivity::class.java)
                    .create().start().resume()
            val testActivity = controller.get()
            val navigationScene = NavigationScene()
            val options = NavigationSceneOptions(TestOnConfigurationChangedRestoreScene::class.java)
            options.setOnlyRestoreVisibleScene(true)
            navigationScene.setInitRootSceneOnCreate(true)
            navigationScene.setArguments(options.toBundle())
            val rootScopeFactory =
                Scope.RootScopeFactory { Scope.DEFAULT_ROOT_SCOPE_FACTORY.rootScope }
            navigationScene.defaultNavigationAnimationExecutor = NoAnimationExecutor()


            val sceneLifecycleManager = SceneLifecycleManager<NavigationScene>()
            sceneLifecycleManager.onActivityCreated(
                testActivity,
                testActivity.mFrameLayout!!,
                navigationScene,
                rootScopeFactory,
                true,
                null
            )

            sceneLifecycleManager.onStart()
            sceneLifecycleManager.onResume()

            val rootScene = navigationScene.currentScene as TestOnConfigurationChangedRestoreScene

            navigationScene.onConfigurationChanged(newConfig)
            Assert.assertEquals(navigationScene.getConfiguration(rootScene), newConfig)

            sceneLifecycleManager.onSaveInstanceState(bundle)
            sceneLifecycleManager.onPause()
            sceneLifecycleManager.onStop()
            sceneLifecycleManager.onDestroyView()
        }

        if (true) {
            val controller =
                Robolectric.buildActivity(TestConfigurationChangedRestoreActivity::class.java)
                    .create().start().resume()
            val testActivity = controller.get()
            val navigationScene = NavigationScene()
            val options = NavigationSceneOptions(TestOnConfigurationChangedRestoreScene::class.java)
            options.setOnlyRestoreVisibleScene(true)
            navigationScene.setInitRootSceneOnCreate(true)
            navigationScene.setArguments(options.toBundle())
            val rootScopeFactory =
                Scope.RootScopeFactory { Scope.DEFAULT_ROOT_SCOPE_FACTORY.rootScope }
            navigationScene.defaultNavigationAnimationExecutor = NoAnimationExecutor()


            val sceneLifecycleManager = SceneLifecycleManager<NavigationScene>()
            sceneLifecycleManager.onActivityCreated(
                testActivity,
                testActivity.mFrameLayout!!,
                navigationScene,
                rootScopeFactory,
                true,
                bundle
            )

            sceneLifecycleManager.onStart()
            sceneLifecycleManager.onResume()

            val rootScene = navigationScene.currentScene as TestOnConfigurationChangedRestoreScene

            //Scene Configuration is reset to default when its view is created, so they are different
            Assert.assertNotEquals(navigationScene.getConfiguration(rootScene), newConfig)

            sceneLifecycleManager.onPause()
            sceneLifecycleManager.onStop()
            sceneLifecycleManager.onDestroyView()
        }
    }
}

class TestConfigurationChangedRestoreActivity : Activity(), IActivity {
    override var mFrameLayout: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        applyOverrideConfiguration(createConfigurationForOrientation(true))
        super.onCreate(savedInstanceState)
        mFrameLayout = FrameLayout(this)
        setContentView(mFrameLayout)
    }
}

class TestOnConfigurationChangedRestoreScene : Scene(), ActivityCompatibleBehavior {
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