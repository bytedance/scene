package com.bytedance.scenedemo.benchmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import com.bytedance.scene.Scene
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.benchmark.performance.PerformanceDemo
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace
import com.bytedance.scenedemo.utility.addTitle


class BenchmarkSamples : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val scrollView = ScrollView(activity)

        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL

        scrollView.addView(layout)

        addClassPathTitle(layout)
        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_basic))

        addButton(layout, getString(R.string.main_nav_btn_compare_activity), View.OnClickListener {
            requireNavigationScene().push(PerformanceDemo::class.java)
        })

        addSpace(layout, 100)

        return scrollView
    }
}