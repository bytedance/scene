package com.bytedance.scenedemo;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import com.bytedance.scene.Scene
import com.bytedance.scene.animation.animatorexecutor.HorizontalTransitionAnimatorExecutor
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scene.navigation.NavigationScene
import com.bytedance.scene.navigation.NavigationSceneOptions
import com.bytedance.scenedemo.multi_stack.MultiStackTabGroupScene
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace
import com.bytedance.scenedemo.utility.addTitle

/**
 * Created by JiangQi on 9/11/18.
 */
class MultiStackDemoScene : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val scrollView = ScrollView(requireSceneContext())

        val layout = LinearLayout(requireSceneContext())
        layout.orientation = LinearLayout.VERTICAL

        scrollView.addView(layout)

        addClassPathTitle(layout)
        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_basic))

        addButton(layout, getString(R.string.nav_multi_stack_btn_1), View.OnClickListener {
            requireNavigationScene().push(NavigationScene::class.java, NavigationSceneOptions(MainScene::class.java, null).toBundle())
        })
        addButton(layout, getString(R.string.nav_multi_stack_btn_2), View.OnClickListener {
            requireNavigationScene().push(NavigationScene::class.java, NavigationSceneOptions(MainScene::class.java, null).toBundle()
                    , PushOptions.Builder().setAnimation(HorizontalTransitionAnimatorExecutor()).build())
        })
        addButton(layout, getString(R.string.nav_multi_stack_btn_3), View.OnClickListener {
            requireNavigationScene().push(MultiStackTabGroupScene::class.java)
        })
        return scrollView
    }
}
