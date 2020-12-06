package com.bytedance.scenedemo.group.hybrid

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.collection.SparseArrayCompat
import androidx.viewpager.widget.ViewPager
import com.bytedance.scene.Scene
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.bytedance.scene.ui.GroupSceneUIUtility
import com.bytedance.scene.ui.template.BottomNavigationViewScene
import com.bytedance.scene.ui.template.NavigationViewScene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil
import com.google.android.material.tabs.TabLayout
import java.util.*
import kotlin.collections.LinkedHashMap


class HybridSample : NavigationViewScene() {
    override fun getMenuResId(): Int {
        return R.menu.drawer_view
    }

    override fun getSceneMap(): LinkedHashMap<Int, Scene> {
        val sparseArrayCompat = LinkedHashMap<Int, Scene>()
        sparseArrayCompat[R.id.nav_camera] = HybridNavigationViewScene.newInstance(0)
        sparseArrayCompat[R.id.nav_gallery] = HybridNavigationViewScene.newInstance(1)
        sparseArrayCompat[R.id.nav_slideshow] = HybridNavigationViewScene.newInstance(2)
        sparseArrayCompat[R.id.nav_manage] = HybridNavigationViewScene.newInstance(3)
        return sparseArrayCompat
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        navigationView.inflateHeaderView(R.layout.nav_header)
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity!!.window.statusBarColor = Color.TRANSPARENT
        }
    }
}

class HybridNavigationViewScene : BottomNavigationViewScene() {
    companion object {
        fun newInstance(index: Int): HybridNavigationViewScene {
            return HybridNavigationViewScene().apply {
                val bundle = Bundle()
                bundle.putInt("index", index)
                setArguments(bundle)
            }
        }
    }

    override fun getMenuResId(): Int {
        return R.menu.bottom_nav_items
    }

    override fun getSceneMap(): LinkedHashMap<Int, Scene> {
        val index = arguments!!["index"] as Int
        val linkedHashMap = LinkedHashMap<Int, Scene>()
        linkedHashMap.put(R.id.menu_home, HybridViewPagerSample.newInstance(index * 10 + 0))
        linkedHashMap.put(R.id.menu_search, HybridViewPagerSample.newInstance(index * 10 + 1))
        linkedHashMap.put(R.id.menu_notifications, HybridViewPagerSample.newInstance(index * 10 + 2))
        return linkedHashMap
    }
}

class HybridViewPagerSample : GroupScene() {
    companion object {
        fun newInstance(index: Int): HybridViewPagerSample {
            return HybridViewPagerSample().apply {
                val bundle = Bundle()
                bundle.putInt("index", index)
                setArguments(bundle)
            }
        }
    }

    private lateinit var mViewPager: ViewPager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.parent_scene_viewpager, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.mViewPager = view.findViewById(R.id.vp)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val tabLayout = requireViewById<TabLayout>(R.id.tablayout)
        tabLayout.setupWithViewPager(mViewPager)

        val index = arguments!!["index"] as Int

        val list = LinkedHashMap<String, UserVisibleHintGroupScene>()

        for (i in 0..3) {
            list[i.toString()] = HybridChildScene.newInstance(index * 100 + i)
        }

        GroupSceneUIUtility.setupWithViewPager(this.mViewPager, this, list)
    }
}

class HybridChildScene : UserVisibleHintGroupScene() {
    companion object {
        fun newInstance(index: Int): HybridChildScene {
            return HybridChildScene().apply {
                val bundle = Bundle()
                bundle.putInt("index", index)
                setArguments(bundle)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        val frameLayout = FrameLayout(requireSceneContext())
        val textView = TextView(requireSceneContext()).apply {
            val index = arguments!!["index"] as Int
            setBackgroundColor(ColorUtil.getMaterialColor(resources, index))
            gravity = Gravity.CENTER
            text = "Child Scene #$index"
        }
        frameLayout.addView(textView)
        return frameLayout
    }
}