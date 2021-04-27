package com.bytedance.scenedemo;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scenedemo.navigation.forresult.SceneResultScene_0
import com.bytedance.scenedemo.navigation.popinterupt.PopInterruptScene
import com.bytedance.scenedemo.navigation.popto.PopToScene
import com.bytedance.scenedemo.navigation.push_clear_current.PushClearCurrentDemoScene
import com.bytedance.scenedemo.navigation.push_pop.PushPopBasicUsageDemoScene
import com.bytedance.scenedemo.navigation.push_singletop.PushSingleTopRootScene
import com.bytedance.scenedemo.navigation.pushandclear.PushClearTaskScene
import com.bytedance.scenedemo.navigation.remove.RemoveDemoScene
import com.bytedance.scenedemo.navigation.reuse.ReuseDemoScene
import com.bytedance.scenedemo.navigation.singletask.SingleTaskDemoScene
import com.bytedance.scenedemo.template.DefaultScene
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace
import com.bytedance.scenedemo.utility.addTitle

/**
 * Created by JiangQi on 8/21/18.
 */
class NavigationListDemo : UserVisibleHintGroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        val scrollView = ScrollView(requireSceneContext())

        val layout = LinearLayout(requireSceneContext())
        layout.orientation = LinearLayout.VERTICAL

        scrollView.addView(layout)

        addClassPathTitle(layout)
        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_basic))

        addButton(layout, getString(R.string.main_nav_btn_push_pop), View.OnClickListener {
            requireNavigationScene().push(PushPopBasicUsageDemoScene::class.java)
        })

        addButton(layout, getString(R.string.main_nav_btn_single_top), View.OnClickListener {
            requireNavigationScene().push(PushSingleTopRootScene::class.java)
        })

        addButton(layout, getString(R.string.main_nav_btn_single_task), View.OnClickListener {
            requireNavigationScene().push(SingleTaskDemoScene::class.java)
        })

        addButton(layout, getString(R.string.main_nav_btn_clear_task), View.OnClickListener {
            requireNavigationScene().push(PushClearTaskScene::class.java)
        })

        addButton(layout, getString(R.string.main_nav_btn_clear_current), View.OnClickListener {
            requireNavigationScene().push(PushClearCurrentDemoScene::class.java)
        })

        addButton(layout, getString(R.string.main_nav_btn_remove), View.OnClickListener {
            requireNavigationScene().push(RemoveDemoScene::class.java)
        })

        addTitle(layout, getString(R.string.main_title_pro));

        addButton(layout, getString(R.string.main_nav_btn_pop_to), View.OnClickListener {
            requireNavigationScene().push(PopToScene::class.java)
        })

        addButton(layout, getString(R.string.main_nav_btn_interrupt_pop), View.OnClickListener {
            requireNavigationScene().push(PopInterruptScene::class.java)
        })

        addButton(layout, getString(R.string.main_nav_btn_for_result), View.OnClickListener {
            requireNavigationScene().push(SceneResultScene_0::class.java)
        })

        addTitle(layout, getString(R.string.main_title_other));

        addButton(layout, getString(R.string.main_nav_btn_reuse), View.OnClickListener {
            requireNavigationScene().push(ReuseDemoScene::class.java)
        })

        addButton(layout, getString(R.string.main_nav_btn_app_compat), View.OnClickListener {
            requireNavigationScene().push(DefaultScene::class.java)
        })

        addTitle(layout, getString(R.string.main_title_todo))
        addButton(layout, getString(R.string.main_nav_btn_deep_link), View.OnClickListener {

        })

        addSpace(layout, 100)

        return scrollView
    }
}
