package com.bytedance.scene.ktx

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.Scene
import junit.framework.Assert
import junit.framework.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


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
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetSceneException() {
        val groupScene = object : Scene() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
                return View(requireActivity())
            }
        }
        createFromSceneLifecycleManager(groupScene)
        groupScene.requireView().getScene()
    }

    @Test
    fun testGetNavigationScene() {
        val groupScene = object : Scene() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
                return View(requireSceneContext())
            }
        }
        val navigationScene = createFromSceneLifecycleManager(groupScene)
        assertSame(navigationScene, groupScene.requireView().getNavigationScene())
    }
}