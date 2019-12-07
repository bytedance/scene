package com.bytedance.scene.ktx

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.Scene
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.IllegalStateException


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ViewExtensionsTests {
    @Test
    fun testGetScene() {
        val groupScene = object : Scene() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
                return View(requireSceneContext())
            }
        }
        createFromSceneLifecycleManager(groupScene)
        assertSame(groupScene, groupScene.requireView().getScene())
        assertSame(groupScene, groupScene.requireView().requireScene())
    }

    @Test(expected = IllegalStateException::class)
    fun testRequireSceneException() {
        val groupScene = object : Scene() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
                return View(requireActivity())
            }
        }
        createFromSceneLifecycleManager(groupScene)
        View(groupScene.requireActivity()).requireScene()
    }

    @Test
    fun testGetNavigationScene() {
        val groupScene = object : Scene() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
                return View(requireSceneContext())
            }
        }
        val navigationScene = createFromSceneLifecycleManager(groupScene)
        assertNotNull(groupScene.requireView().getNavigationScene())
        assertSame(navigationScene, groupScene.requireView().requireNavigationScene())
    }
}