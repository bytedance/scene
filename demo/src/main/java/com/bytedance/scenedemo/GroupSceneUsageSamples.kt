package com.bytedance.scenedemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.bytedance.scenedemo.group.async_inflate.AsyncInflateSceneDemo
import com.bytedance.scenedemo.group.basic_usage.GroupSceneBasicUsageSample
import com.bytedance.scenedemo.group.drawer.NavigationViewSample
import com.bytedance.scenedemo.group.hybrid.HybridSample
import com.bytedance.scenedemo.group.inherited.InheritedDemo
import com.bytedance.scenedemo.group.placeholder.PlaceHolderViewSample
import com.bytedance.scenedemo.group.tab.BottomNavigationViewSample
import com.bytedance.scenedemo.group.viewpager.ViewPagerSample
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace
import com.bytedance.scenedemo.utility.addTitle

class GroupSceneUsageSamples : UserVisibleHintGroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        val scrollView = ScrollView(requireSceneContext())

        val layout = LinearLayout(requireSceneContext())
        layout.orientation = LinearLayout.VERTICAL

        scrollView.addView(layout)

        addClassPathTitle(layout)
        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_basic))

        addButton(layout, getString(R.string.main_part_btn_child_scene), View.OnClickListener {
            requireNavigationScene().push(GroupSceneBasicUsageSample::class.java)
        })

        addButton(layout, getString(R.string.main_part_btn_scene_place_holder_view), View.OnClickListener {
            requireNavigationScene().push(PlaceHolderViewSample::class.java)
        })

        addButton(layout, getString(R.string.main_part_btn_tab_view_pager), View.OnClickListener {
            requireNavigationScene().push(ViewPagerSample::class.java)
        })

        addButton(layout, getString(R.string.main_part_btn_drawer), View.OnClickListener {
            requireNavigationScene().push(NavigationViewSample::class.java)
        })

        addButton(layout, getString(R.string.main_part_btn_bottom_tab_layout), View.OnClickListener {
            requireNavigationScene().push(BottomNavigationViewSample::class.java)
        })

        addButton(layout, getString(R.string.main_part_btn_hybrid_layout), View.OnClickListener {
            requireNavigationScene().push(HybridSample::class.java)
        })

        addTitle(layout, getString(R.string.main_title_pro));

        addButton(layout, getString(R.string.main_part_btn_async_inflate), View.OnClickListener {
            requireNavigationScene().push(AsyncInflateSceneDemo::class.java)
        })

        addButton(layout, getString(R.string.main_part_btn_inherited_scene), View.OnClickListener {
            requireNavigationScene().push(InheritedDemo::class.java)
        })

        addSpace(layout, 100)

        return scrollView
    }
}