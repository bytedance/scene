package com.bytedance.scene.ktx

import android.os.Bundle
import android.os.Looper.getMainLooper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.Scene
import com.bytedance.scene.ktx.utility.TestActivity
import com.bytedance.scene.ktx.utility.TestAppCompatActivity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@LooperMode(LooperMode.Mode.PAUSED)
class SceneExtensionsTests {
    @Test
    fun testPostAfterDestroy() {
        val called = AtomicBoolean(false)
        val scene = object : Scene() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
                return View(requireSceneContext())
            }

            override fun onActivityCreated(savedInstanceState: Bundle?) {
                super.onActivityCreated(savedInstanceState)
                post(Runnable {
                    called.set(true)
                })
            }
        }

        val manager = createFromInitSceneLifecycleManager(scene).first
        manager.onStart()
        manager.onResume()
        manager.onPause()
        manager.onStop()
        manager.onDestroyView()
        shadowOf(getMainLooper()).idle()//execute Handler posted task
        assertFalse(called.get())

        scene.post(Runnable {
            called.set(true)
        })

        shadowOf(getMainLooper()).idle()
        assertFalse(called.get())
    }

    @Test
    fun testPost() {
        val called = AtomicBoolean(false)
        val scene = object : Scene() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
                return View(requireSceneContext())
            }

            override fun onActivityCreated(savedInstanceState: Bundle?) {
                super.onActivityCreated(savedInstanceState)
                post(Runnable {
                    called.set(true)
                })
            }
        }

        createFromSceneLifecycleManager(scene)
        shadowOf(getMainLooper()).idle()//execute Handler posted task
        assertTrue(called.get())
    }

    @Test
    fun testPostDelay() {
        val called = AtomicBoolean(false)
        val scene = object : Scene() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
                return View(requireSceneContext())
            }

            override fun onActivityCreated(savedInstanceState: Bundle?) {
                super.onActivityCreated(savedInstanceState)
                postDelayed(Runnable {
                    called.set(true)
                }, 1000);
            }
        }

        createFromSceneLifecycleManager(scene)
        SystemClock.sleep(2000)
        shadowOf(getMainLooper()).idle()//execute Handler posted task
        assertTrue(called.get())
    }

    @Test
    fun testPostDelayAfterDestroy() {
        val called = AtomicBoolean(false)
        val scene = object : Scene() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
                return View(requireSceneContext())
            }

            override fun onActivityCreated(savedInstanceState: Bundle?) {
                super.onActivityCreated(savedInstanceState)
                postDelayed(Runnable {
                    called.set(true)
                }, 1000);
            }
        }

        val manager = createFromInitSceneLifecycleManager(scene).first
        manager.onStart()
        manager.onResume()
        manager.onPause()
        manager.onStop()
        manager.onDestroyView()
        SystemClock.sleep(2000)
        shadowOf(getMainLooper()).idle()//execute Handler posted task
        assertFalse(called.get())

        scene.postDelayed(Runnable {
            called.set(true)
        }, 1000)

        SystemClock.sleep(2000)
        shadowOf(getMainLooper()).idle()//execute Handler posted task
        assertFalse(called.get())
    }

    @Test
    fun testIsDestroyed() {
        val scene = object : Scene() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
                return View(requireSceneContext())
            }
        }
        assertFalse(scene.isDestroyed())

        val manager = createFromInitSceneLifecycleManager(scene).first
        assertFalse(scene.isDestroyed())
        manager.onStart()
        assertFalse(scene.isDestroyed())
        manager.onResume()
        assertFalse(scene.isDestroyed())
        manager.onPause()
        assertFalse(scene.isDestroyed())
        manager.onStop()
        assertFalse(scene.isDestroyed())
        manager.onDestroyView()
        assertTrue(scene.isDestroyed())
    }

    @Test
    fun testGetFragmentActivityNull() {
        val scene = object : Scene() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
                return View(requireSceneContext())
            }
        }
        assertNull(scene.fragmentActivity())
    }

    @Test(expected = ClassCastException::class)
    fun testGetFragmentActivityCastError() {
        val scene = object : Scene() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
                return View(requireSceneContext())
            }
        }
        val manager = createFromInitSceneLifecycleManager(TestActivity::class.java, scene).first
        scene.fragmentActivity()
    }

    @Test
    fun testGetFragmentActivity() {
        val scene = object : Scene() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
                return View(requireSceneContext())
            }
        }
        val manager = createFromInitSceneLifecycleManager(TestAppCompatActivity::class.java, scene).first
        assertNotNull(scene.fragmentActivity())
        assertNotNull(scene.requireFragmentActivity())
    }
}