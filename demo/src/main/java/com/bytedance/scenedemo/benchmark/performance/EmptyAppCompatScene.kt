package com.bytedance.scenedemo.benchmark.performance

import android.graphics.Color
import com.bytedance.scene.ui.template.AppCompatScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import com.bytedance.scenedemo.benchmark.performance.PerformanceDemo
import android.widget.Toast
import com.bytedance.scenedemo.R

/**
 * Created by JiangQi on 8/21/18.
 */
class EmptyAppCompatScene : AppCompatScene() {
    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        val view = View(activity)
        view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                view.viewTreeObserver.removeOnPreDrawListener(this)
                val a = System.currentTimeMillis() - PerformanceDemo.startTimestamp
                Toast.makeText(activity, getString(R.string.nav_compare_tip, a), Toast.LENGTH_SHORT).show()
                return true
            }
        })
        view.setBackgroundColor(Color.RED)
        return view
    }
}