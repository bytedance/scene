package com.bytedance.scene.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.Scene
import com.bytedance.scene.interfaces.PopOptions
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.navigation.reuse.IReusePool
import com.bytedance.scene.navigation.reuse.IReuseScene
import com.bytedance.scene.navigation.reuse.ReuseBehavior
import com.bytedance.scene.navigation.reuse.ReuseState
import com.bytedance.scene.navigation.reuse.isReleased
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.ConcurrentHashMap

/**
 * Test reuse related capabilities
 *
 * Created by zhuqingying on 2025/2/11
 * @author zhuqingying@bytedance.com
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NavigationSceneReuseTests {
    internal class ReuseScene : Scene(), IReuseScene {
        var reuse = false

        override fun isReusable(): Boolean {
            return reuse
        }

        override fun onPrepare(bundle: Bundle?) {
            print("onPrepare")
        }
        override fun onRelease() {
            print("onRelease")
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup, savedInstanceState: Bundle?
        ): View {
            return View(requireSceneContext())
        }
    }

    @Test
    fun testReuseSceneWithReusableTrue() {
        val rootScene: Scene = object : Scene() {
            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup,
                savedInstanceState: Bundle?
            ): View {
                return View(requireSceneContext())
            }
        }
        val navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(rootScene)
        navigationScene.defaultNavigationAnimationExecutor = null
        val reuseScene = ReuseScene().apply { reuse = true }

        navigationScene.push(reuseScene)
        Assert.assertFalse(reuseScene.isReleased()) // Should be not released after push

        navigationScene.remove(reuseScene)
        Assert.assertTrue(reuseScene.isReleased()) // Should be released after removal

        navigationScene.push(
            ReuseScene::class.java,
            null,
            PushOptions.Builder().setUseSceneFromReusePool(true).build()
        )

        Assert.assertSame(reuseScene, navigationScene.currentScene)
        Assert.assertFalse(reuseScene.isReleased())  // Should be not released after reuse
    }

    @Test
    fun testReuseSceneWithReusableTrueWithUsePost() {
        val rootScene: Scene = object : Scene() {
            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup,
                savedInstanceState: Bundle?
            ): View {
                return View(requireSceneContext())
            }
        }
        val navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(rootScene)
        navigationScene.defaultNavigationAnimationExecutor = null
        val reuseScene = ReuseScene().apply { reuse = true }

        navigationScene.push(reuseScene)
        Assert.assertFalse(reuseScene.isReleased()) // Should be not released after push

        navigationScene.remove(reuseScene)
        Assert.assertTrue(reuseScene.isReleased()) // Should be released after removal

        navigationScene.push(
            ReuseScene::class.java,
            null,
            PushOptions.Builder()
                .setUseSceneFromReusePool(true)
                .setUsePost(true)
                .build()
        )

        flushUI()

        Assert.assertSame(reuseScene, navigationScene.currentScene)
        Assert.assertFalse(reuseScene.isReleased())  // Should be not released after reuse
    }

    @Test
    fun testReuseSceneWithReusableFalse() {
        val rootScene: Scene = object : Scene() {
            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup,
                savedInstanceState: Bundle?
            ): View {
                return View(requireSceneContext())
            }
        }
        val navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(rootScene)
        navigationScene.defaultNavigationAnimationExecutor = null
        val reuseScene = ReuseScene().apply { reuse = false }

        navigationScene.push(reuseScene)
        Assert.assertFalse(reuseScene.isReleased()) // Should be not released after push

        navigationScene.remove(reuseScene)
        Assert.assertTrue(reuseScene.isReleased()) // Should be released after removal

        navigationScene.push(
            ReuseScene::class.java,
            null,
            PushOptions.Builder().setUseSceneFromReusePool(true).build()
        )

        Assert.assertNotSame(reuseScene, navigationScene.currentScene)
        Assert.assertTrue(reuseScene.isReleased())  // not reuse，original scene should keep released
    }

    @Test
    fun testReuseSceneWithReusableFalseWithUsePost() {
        val rootScene: Scene = object : Scene() {
            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup,
                savedInstanceState: Bundle?
            ): View {
                return View(requireSceneContext())
            }
        }
        val navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(rootScene)
        navigationScene.defaultNavigationAnimationExecutor = null
        val reuseScene = ReuseScene().apply { reuse = false }

        navigationScene.push(reuseScene)
        Assert.assertFalse(reuseScene.isReleased())

        navigationScene.remove(reuseScene)
        Assert.assertTrue(reuseScene.isReleased())

        navigationScene.push(
            ReuseScene::class.java,
            null,
            PushOptions.Builder()
                .setUseSceneFromReusePool(true)
                .setUsePost(true)
                .build()
        )

        flushUI()

        Assert.assertNotSame(reuseScene, navigationScene.currentScene)
        Assert.assertTrue(reuseScene.isReleased())
    }

    @Test
    fun testReuseSceneLifecycleWithUseIdleWhenStop() {
        testReuseSceneLifecycle(PopOptions.Builder().setUseIdleWhenStop(true).build())
    }

    @Test
    fun testReuseSceneLifecycleWithUsePost() {
        testReuseSceneLifecycle(PopOptions.Builder().setUsePost(true).build())
    }

    @Test
    fun testReuseSceneLifecycleWithUsePostWhenPause() {
        testReuseSceneLifecycle(PopOptions.Builder().setUsePostWhenPause(true).build())
    }

    @Test
    fun testReuseSceneLifecycleWithUseActivityCompatibleLifeyclce() {
        testReuseSceneLifecycle(
            PopOptions.Builder().setUseActivityCompatibleLifecycle(true).build()
        )
    }

    @Test
    fun testReuseSceneNavigationAbility() {
        val rootScene: Scene = object : Scene() {
            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup,
                savedInstanceState: Bundle?
            ): View {
                return View(requireSceneContext())
            }
        }
        val navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(rootScene)
        navigationScene.defaultNavigationAnimationExecutor = null
        val reuseScene = ReuseScene().apply { reuse = true }
        navigationScene.push(reuseScene)
        navigationScene.remove(reuseScene)
        navigationScene.push(
            ReuseScene::class.java,
            null,
            PushOptions
                .Builder()
                .setUseSceneFromReusePool(true)
                .setReuseBehavior { candidate ->
                    candidate.javaClass == ReuseScene::class.java
                }
                .build()
        )
        Assert.assertSame(reuseScene, navigationScene.currentScene)
    }

    private fun testReuseSceneLifecycle(popOptions: PopOptions) {
        val rootScene: Scene = object : Scene() {
            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup,
                savedInstanceState: Bundle?
            ): View {
                return View(requireSceneContext())
            }
        }
        val navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(rootScene)
        navigationScene.defaultNavigationAnimationExecutor = null
        val reuseScene = spyk(ReuseScene().apply { reuse = true })
        navigationScene.push(reuseScene)
        navigationScene.pop(popOptions)
        ShadowLooper.idleMainLooper()
        verifyOrder {
            reuseScene.onStop()
            reuseScene.onRelease()
        }
        navigationScene.push(
            ReuseScene::class.java,
            null,
            PushOptions.Builder().setUseSceneFromReusePool(true).build()
        )
        Assert.assertSame(reuseScene, navigationScene.currentScene)
        verifyOrder {
            reuseScene.onPrepare(any())
            reuseScene.onStart()
        }
        navigationScene.remove(reuseScene)
        verify(exactly = 0) {
            reuseScene.onDestroyView()
        }
        navigationScene.onDestroyView()
        verify(exactly = 1) {
            reuseScene.onDestroyView()
        }
    }

    // Test Cache Pool
    @Test
    fun testCustomReusePool() {
        val rootScene: Scene = object : Scene() {
            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup,
                savedInstanceState: Bundle?
            ): View {
                return View(requireSceneContext())
            }
        }

        // Create a custom reuse pool to track method calls
        val trackingPool = object : IReusePool {
            val reusePool: ArrayDeque<IReuseScene> = ArrayDeque()
            val reuseStates = ConcurrentHashMap<IReuseScene, ReuseState>()

            override fun releaseScene(scene: IReuseScene): Boolean {
                if (!scene.isReusable()) {
                    return false
                }

                check(!reusePool.contains(scene)) { "Scene already in reuse pool" }

                reusePool.addLast(scene)
                reuseStates[scene] = ReuseState.RELEASED
                return true
            }

            override fun reuseScene(behavior: ReuseBehavior): Scene? {
                // Find the first matching scene
                val matchedScene = reusePool.find { behavior.match(it) } ?: return null

                // Remove from the pool
                reusePool.remove(matchedScene)
                reuseStates[matchedScene] = ReuseState.REUSED
                return matchedScene as Scene
            }


            override fun removeScenes(behavior: ReuseBehavior): List<IReuseScene> {
                // Find all the matching scenes first
                val scenesToRemove = reusePool.filter { behavior.match(it) }

                // Then remove these scenes from the pool
                reusePool.removeAll(scenesToRemove.toSet())

                return scenesToRemove
            }

            override fun getSceneState(scene: IReuseScene): ReuseState {
                return reuseStates[scene] ?: ReuseState.INITED
            }

            override fun clear() {
                reusePool.clear()
                reuseStates.clear()
            }
        }

        val navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(rootScene, trackingPool)
        navigationScene.defaultNavigationAnimationExecutor = null

        // Verify setting successfully
        Assert.assertSame(trackingPool, navigationScene.reusePool)
        Assert.assertTrue(trackingPool.reusePool.isEmpty())

        // Test reuse logic
        val reuseScene = ReuseScene().apply { reuse = true }
        navigationScene.push(reuseScene)
        Assert.assertFalse(reuseScene.isReleased())

        navigationScene.remove(reuseScene)
        Assert.assertTrue(reuseScene.isReleased())

        // Verify that scene is added to the custom reuse pool
        Assert.assertEquals(1, trackingPool.reusePool.size)
        Assert.assertTrue(trackingPool.reusePool.contains(reuseScene))

        // Obtain from the reuse pool
        navigationScene.push(
            ReuseScene::class.java,
            null,
            PushOptions.Builder().setUseSceneFromReusePool(true).build()
        )

        // Verify that the scene is reused
        Assert.assertSame(reuseScene, navigationScene.currentScene)
        Assert.assertTrue(trackingPool.reusePool.isEmpty())
    }

    @Test
    fun testCustomReusePoolWithSpecialBehavior() {
        val rootScene: Scene = object : Scene() {
            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup,
                savedInstanceState: Bundle?
            ): View {
                return View(requireSceneContext())
            }
        }

        // Implement a reuse pool with special behavior
        // - limited to a maximum size of 1
        val limitedSizeReusePool = object : IReusePool {
            private val reusePool = ArrayDeque<IReuseScene>()
            private val reuseStates = HashMap<IReuseScene, ReuseState>()
            private var size = 0

            override fun releaseScene(scene: IReuseScene): Boolean {
                if (!scene.isReusable()) {
                    return false
                }

                if (size >= 1) {
                    // The pool is full and no new scenes are accepted
                    return false
                }

                reusePool.addLast(scene)
                reuseStates[scene] = ReuseState.RELEASED
                size++
                return true
            }

            override fun reuseScene(behavior: ReuseBehavior): Scene? {
                val matchedScene = reusePool.find { behavior.match(it) } ?: return null

                reusePool.remove(matchedScene)
                reuseStates[matchedScene] = ReuseState.REUSED
                size--
                return matchedScene as Scene
            }

            override fun removeScenes(behavior: ReuseBehavior): List<IReuseScene> {
                val scenesToRemove = reusePool.filter { behavior.match(it) }
                reusePool.removeAll(scenesToRemove.toSet())
                size -= scenesToRemove.size
                return scenesToRemove
            }

            override fun getSceneState(scene: IReuseScene): ReuseState {
                return reuseStates[scene] ?: ReuseState.INITED
            }

            override fun clear() {
                reusePool.clear()
                reuseStates.clear()
                size = 0
            }
        }
        val navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(rootScene, limitedSizeReusePool)
        navigationScene.defaultNavigationAnimationExecutor = null

        // Add two reusable scenes
        val reuseScene1 = ReuseScene().apply { reuse = true }
        val reuseScene2 = ReuseScene().apply { reuse = true }

        navigationScene.push(reuseScene1)
        navigationScene.remove(reuseScene1)

        // The first one should be successfully added to the reuse pool
        Assert.assertTrue(navigationScene.isReleased(reuseScene1))

        navigationScene.push(reuseScene2)
        navigationScene.remove(reuseScene2)

        // The second one should not be added to the reuse pool because the reuse pool is full
        Assert.assertFalse(navigationScene.isReleased(reuseScene2))

        // Test reuse
        navigationScene.push(
            ReuseScene::class.java,
            null,
            PushOptions.Builder().setUseSceneFromReusePool(true).build()
        )

        // Reuse should be the first scene
        Assert.assertSame(reuseScene1, navigationScene.currentScene)

        // The reuse pool should be empty
        navigationScene.push(
            ReuseScene::class.java,
            null,
            PushOptions.Builder().setUseSceneFromReusePool(true).build()
        )

        // New scenarios should be created, not reused
        Assert.assertNotSame(reuseScene1, navigationScene.currentScene)
        Assert.assertNotSame(reuseScene2, navigationScene.currentScene)
    }


    protected fun flushUI() {
        Robolectric.flushForegroundThreadScheduler()
    }
}
