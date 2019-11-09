package com.bytedance.scenedemo.group.viewpager

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.bytedance.scene.ui.GroupSceneUIUtility
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil
import com.google.android.material.tabs.TabLayout
import java.util.*

/**
 * Created by JiangQi on 7/30/18.
 */
class ViewPagerSample : GroupScene() {

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

        val list = LinkedHashMap<String, UserVisibleHintGroupScene>()

        for (i in 0..3) {
            list[i.toString()] = ViewPagerChildScene.newInstance(i)
        }

        GroupSceneUIUtility.setupWithViewPager(this.mViewPager, this, list)
    }
}

class ViewPagerChildScene : UserVisibleHintGroupScene() {
    companion object {
        fun newInstance(index: Int): ViewPagerChildScene {
            return ViewPagerChildScene().apply {
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