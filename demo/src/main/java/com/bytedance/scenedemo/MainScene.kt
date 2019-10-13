package com.bytedance.scenedemo

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import com.bytedance.scene.Scene
import com.bytedance.scene.ui.template.NavigationViewScene
import com.bytedance.scenedemo.activity_compatibility.ActivityCompatibilitySamples
import com.bytedance.scenedemo.animation.AnimationListDemoScene
import com.bytedance.scenedemo.architecture_patterns.PatternsSamples
import com.bytedance.scenedemo.dialog.DialogListDemoScene
import com.bytedance.scenedemo.multi_stack.MultiStackDemoScene
import com.bytedance.scenedemo.router.RouterSample


class MainScene : NavigationViewScene() {
    override fun getMenuResId(): Int {
        return R.menu.navigation_view
    }

    override fun getSceneMap(): LinkedHashMap<Int, Scene> {
        val map = LinkedHashMap<Int, Scene>()
        map[R.id.navigation] = NavigationListDemo()
        map[R.id.navigation_multi_stack] = MultiStackDemoScene()
        map[R.id.navigation_router] = RouterSample()
        map[R.id.group] = GroupListDemo()
        map[R.id.dialog] = DialogListDemoScene()
        map[R.id.animation] = AnimationListDemoScene()
        map[R.id.architecture_patterns] = PatternsSamples()
        map[R.id.activity_compatibility] = ActivityCompatibilitySamples()
        map[R.id.extreme_case] = CaseListDemo()
        return map
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val window = requireActivity().window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
        }
    }
}