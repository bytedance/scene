package com.bytedance.scenedemo.multi_stack

import com.bytedance.scene.ui.template.BottomNavigationViewScene
import com.bytedance.scenedemo.R
import android.os.Bundle
import com.bytedance.scene.Scene
import com.bytedance.scene.navigation.NavigationScene
import com.bytedance.scene.utlity.SceneInstanceUtility
import com.bytedance.scene.navigation.NavigationSceneOptions
import com.bytedance.scenedemo.multi_stack.MultiStackTabChildScene
import java.util.LinkedHashMap

class MultiStackTabGroupScene : BottomNavigationViewScene() {
    override fun getMenuResId(): Int {
        return R.menu.bottom_nav_items
    }

    private fun getBundle(index: Int): Bundle {
        val bundle = Bundle()
        bundle.putInt("index", index)
        return bundle
    }

    override fun getSceneMap(): LinkedHashMap<Int, Scene> {
        val linkedHashMap = LinkedHashMap<Int, Scene>()
        val bundle = Bundle()
        bundle.putInt("index", 0)
        var navigationScene = SceneInstanceUtility.getInstanceFromClass(
            NavigationScene::class.java,
            NavigationSceneOptions(MultiStackTabChildScene::class.java, getBundle(0)).toBundle()
        ) as NavigationScene
        linkedHashMap[R.id.menu_home] = navigationScene
        navigationScene = SceneInstanceUtility.getInstanceFromClass(
            NavigationScene::class.java,
            NavigationSceneOptions(MultiStackTabChildScene::class.java, getBundle(1)).toBundle()
        ) as NavigationScene
        linkedHashMap[R.id.menu_search] = navigationScene
        navigationScene = SceneInstanceUtility.getInstanceFromClass(
            NavigationScene::class.java,
            NavigationSceneOptions(MultiStackTabChildScene::class.java, getBundle(2)).toBundle()
        ) as NavigationScene
        linkedHashMap[R.id.menu_notifications] = navigationScene
        return linkedHashMap
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}