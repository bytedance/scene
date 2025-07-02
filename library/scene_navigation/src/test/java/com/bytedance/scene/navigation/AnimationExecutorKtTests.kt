package com.bytedance.scene.navigation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.os.Bundle
import android.os.Looper.getMainLooper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bytedance.scene.Scene
import com.bytedance.scene.SceneComponentFactory
import com.bytedance.scene.SceneLifecycleManager
import com.bytedance.scene.Scope
import com.bytedance.scene.Scope.RootScopeFactory
import com.bytedance.scene.State
import com.bytedance.scene.animation.AnimationInfo
import com.bytedance.scene.animation.NavigationAnimatorExecutor
import com.bytedance.scene.animation.animatorexecutor.MutableAnimationExecutor
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor
import com.bytedance.scene.interfaces.PopOptions
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.navigation.post.NavigationSourceSupportPostUtility
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * Created by junyu on 2025/5/12
 * @author yuejunyu.0@tiktok.com
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@LooperMode(LooperMode.Mode.PAUSED)
class AnimationExecutorKtTests {
    @Test
    fun testDoDestroyAfterAnimationWhenNotRemoveViewOptionEnabled() {
        val actual = commonTestDoDestroyAfterAnimationWhenNotRemoveViewOption(true)
        val expected = listOf(POP_ANIM_END, VIEW_DESTROY)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testDoDestroyAfterAnimationWhenNotRemoveViewOptionDisabled() {
        val actual = commonTestDoDestroyAfterAnimationWhenNotRemoveViewOption(false)
        val expected = listOf(VIEW_DESTROY, POP_ANIM_END)
        Assert.assertEquals(expected, actual)
    }

    private fun commonTestDoDestroyAfterAnimationWhenNotRemoveViewOption(enabled: Boolean): List<String> {
        val rootScene = SimpleScene()
        val navigationScene = createFromInitSceneLifecycleManager(rootScene)
        val simpleScene = SimpleScene()
        navigationScene.push(simpleScene)
        shadowOf(getMainLooper()).idle()

        val tasks = arrayListOf<String>()
        val popAnimator = simpleAnimator {
            tasks += POP_ANIM_END
        }
        simpleScene.onDestroyView = {
            tasks += VIEW_DESTROY
        }

        val popOptions = PopOptions.Builder().setUsePost(true)
            .setAnimation(popAnimator).setStopAfterAnimation(enabled).build()
        navigationScene.pop(popOptions)
        shadowOf(getMainLooper()).idle()

        Assert.assertEquals(State.NONE, simpleScene.state)
        return tasks
    }

    @Test
    fun testMutablePushAnimation() {
        val pushMutableAnimation = MutableAnimationExecutor(NoAnimationExecutor())
        val rootScene = SimpleScene()
        val navigationScene = createFromInitSceneLifecycleManager(rootScene)
        val simpleScene = SimpleScene()

        var mutPushAnimExecuted = false
        simpleScene.onActivityCreated = {
            pushMutableAnimation.delegated = simpleAnimator {
                mutPushAnimExecuted = true
            }
        }

        val pushOptions = PushOptions.Builder().setAnimation(pushMutableAnimation).build()
        navigationScene.push(simpleScene, pushOptions)
        shadowOf(getMainLooper()).idle()

        Assert.assertTrue(mutPushAnimExecuted)
    }

    @Test
    fun testMutablePopAnimation() {
        val popMutableAnimation = MutableAnimationExecutor(NoAnimationExecutor())
        val rootScene = SimpleScene()
        val navigationScene = createFromInitSceneLifecycleManager(rootScene)
        val simpleScene = SimpleScene()

        var mutablePopAnimExecuted = false
        simpleScene.onActivityCreated = {
            popMutableAnimation.delegated = simpleAnimator {
                mutablePopAnimExecuted = true
            }
        }

        navigationScene.push(simpleScene)
        shadowOf(getMainLooper()).idle()

        val popOptions = PopOptions.Builder().setUsePost(true).setAnimation(popMutableAnimation).build()
        navigationScene.pop(popOptions)
        shadowOf(getMainLooper()).idle()

        Assert.assertTrue(mutablePopAnimExecuted)
    }

    /**
     * sorry for copy from [NavigationSourceSupportPostUtility]...
     * the code from [NavigationSourceSupportPostUtility] not let scene become visible,
     * so the scene's width and height is not available. I must need to let them visible
     * to test my animation changes.
     */
    private fun createFromInitSceneLifecycleManager(rootScene: Scene): NavigationScene {
        val controller: ActivityController<TestActivity> =
            Robolectric.buildActivity(TestActivity::class.java).create().start().resume().visible()
        val testActivity = controller.get()
        val navigationScene = NavigationScene()
        val options = NavigationSceneOptions(rootScene.javaClass)
        options.setOnlyRestoreVisibleScene(true)
        navigationScene.setArguments(options.toBundle())

        val rootScopeFactory = RootScopeFactory { Scope.DEFAULT_ROOT_SCOPE_FACTORY.rootScope }

        val sceneComponentFactory = SceneComponentFactory { _, className, _ ->
            if (className == rootScene.javaClass.name) {
                return@SceneComponentFactory rootScene
            }
            null
        }

        navigationScene.setRootSceneComponentFactory(sceneComponentFactory)

        val sceneLifecycleManager = SceneLifecycleManager<NavigationScene>()
        sceneLifecycleManager.onActivityCreated(
            testActivity, testActivity.mFrameLayout!!,
            navigationScene, rootScopeFactory,
            false, null
        )
        sceneLifecycleManager.onStart()
        sceneLifecycleManager.onResume()
        return navigationScene
    }

    class TestActivity : Activity() {
        var mFrameLayout: FrameLayout? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            mFrameLayout = FrameLayout(this)
            setContentView(mFrameLayout)
        }
    }

    class SimpleScene : Scene() {
        var onActivityCreated: (() -> Unit)? = null
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            onActivityCreated?.invoke()
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            return View(requireSceneContext())
        }

        var onDestroyView: (() -> Unit)? = null
        override fun onDestroyView() {
            super.onDestroyView()
            onDestroyView?.invoke()
        }
    }
}

private const val POP_ANIM_END = "popAnimFinished"
private const val VIEW_DESTROY = "onDestroyView"

private fun simpleAnimator(onEndAction: () -> Unit) = object : NavigationAnimatorExecutor() {
    override fun isSupport(from: Class<out Scene>, to: Class<out Scene>): Boolean = true

    override fun onPopAnimator(fromInfo: AnimationInfo?, toInfo: AnimationInfo?): Animator {
        val anim = ValueAnimator.ofFloat(0f, 1f).setDuration(1L)
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onEndAction()
            }
        })
        return anim
    }

    override fun onPushAnimator(fromInfo: AnimationInfo?, toInfo: AnimationInfo?): Animator {
        val anim = ValueAnimator.ofFloat(0f, 1f).setDuration(1L)
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onEndAction()
            }
        })
        return anim
    }
}
