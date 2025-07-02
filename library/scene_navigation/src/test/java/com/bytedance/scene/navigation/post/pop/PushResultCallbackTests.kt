package com.bytedance.scene.navigation.post.pop

import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.Scene
import com.bytedance.scene.SceneLifecycleManager
import com.bytedance.scene.Scope
import com.bytedance.scene.Scope.RootScopeFactory
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor
import com.bytedance.scene.interfaces.PopOptions
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.navigation.NavigationScene
import com.bytedance.scene.navigation.NavigationSceneGetter.requireNavigationScene
import com.bytedance.scene.navigation.NavigationSceneOptions
import com.bytedance.scene.navigation.NavigationSourceUtility
import com.bytedance.scene.navigation.post.saveandrestore.OnlyRestoreVisibleSceneTests
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.UUID

/**
 * Created by sunyongsheng.aengus on 2025/3/27
 * @author sunyongsheng.aengus@bytedance.com
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PushResultCallbackTests {

    /**
     * Ensure navigationScene.currentScene is correct in pushResultCallback
     */
    @Test
    fun popWithUsePost() {
        val sceneLifecycleManager = SceneLifecycleManager<NavigationScene>()
        val navigationScene = NavigationScene()
        val controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity::class.java)
            .create()
            .start()
            .resume()
        val testActivity = controller.get()
        val rootScene = TestScene()
        val options = NavigationSceneOptions(TestScene::class.java)
        options.setOnlyRestoreVisibleScene(true)
        options.setFixOnResultTiming(true)
        navigationScene.setArguments(options.toBundle())
        navigationScene.setRootSceneComponentFactory { cl, className, bundle ->
            rootScene
        }

        val rootScopeFactory = RootScopeFactory { Scope.DEFAULT_ROOT_SCOPE_FACTORY.rootScope }

        navigationScene.defaultNavigationAnimationExecutor = NoAnimationExecutor()

        sceneLifecycleManager.onActivityCreated(
            testActivity, testActivity.mFrameLayout,
            navigationScene, rootScopeFactory,
            true, null
        )
        sceneLifecycleManager.onStart()
        sceneLifecycleManager.onResume()

        val secondScene = TestScene()
        val secondId = secondScene.id
        var pushResultCallback = false
        val builder = PushOptions.Builder()
            .setPushResultCallback {
                pushResultCallback = true
                Assert.assertEquals(secondId, it)
                Assert.assertEquals(navigationScene.currentScene, rootScene)
            }
        navigationScene.push(secondScene, builder.build())
        navigationScene.pop(PopOptions.Builder().setUsePost(true).build())

        ShadowLooper.runUiThreadTasks()

        Assert.assertTrue(pushResultCallback)
    }

    /**
     * this test will be remove when [NavigationSceneOptions.setFixOnResultTiming] is set to true
     */
    @Test
    fun popWithUsePost_incorrect() {
        val sceneLifecycleManager = SceneLifecycleManager<NavigationScene>()
        val navigationScene = NavigationScene()
        val controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity::class.java)
            .create()
            .start()
            .resume()
        val testActivity = controller.get()
        val rootScene = TestScene()
        val options = NavigationSceneOptions(TestScene::class.java)
        options.setOnlyRestoreVisibleScene(true)
        options.setFixOnResultTiming(false)
        navigationScene.setArguments(options.toBundle())
        navigationScene.setRootSceneComponentFactory { cl, className, bundle ->
            rootScene
        }

        val rootScopeFactory = RootScopeFactory { Scope.DEFAULT_ROOT_SCOPE_FACTORY.rootScope }

        navigationScene.defaultNavigationAnimationExecutor = NoAnimationExecutor()

        sceneLifecycleManager.onActivityCreated(
            testActivity, testActivity.mFrameLayout,
            navigationScene, rootScopeFactory,
            true, null
        )
        sceneLifecycleManager.onStart()
        sceneLifecycleManager.onResume()

        val secondScene = TestScene()
        val secondId = secondScene.id
        var pushResultCallback = false
        val builder = PushOptions.Builder()
            .setPushResultCallback {
                pushResultCallback = true
                Assert.assertEquals(secondId, it)
                Assert.assertEquals(navigationScene.currentScene, secondScene)
            }
        navigationScene.push(secondScene, builder.build())
        navigationScene.pop(PopOptions.Builder().setUsePost(true).build())

        ShadowLooper.runUiThreadTasks()

        Assert.assertTrue(pushResultCallback)
    }

    class TestScene : Scene() {
        val id = UUID.randomUUID().toString()

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            return View(requireSceneContext())
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            requireNavigationScene(this).setResult(this, id)
        }
    }
}