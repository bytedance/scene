package com.bytedance.scenedemo.benchmark.performance

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.bytedance.scenedemo.R
import android.widget.TextView
import com.bytedance.scenedemo.benchmark.performance.PerformanceDemo
import android.content.Intent
import android.view.View
import android.widget.Button
import com.bytedance.scene.Scene
import com.bytedance.scene.ktx.navigationScene
import com.bytedance.scenedemo.benchmark.performance.EmptyAppCompatActivity
import com.bytedance.scenedemo.benchmark.performance.EmptyAppCompatScene
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/21/18.
 */
class PerformanceDemo : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 0))
        val name = view.findViewById<TextView>(R.id.name)
        name.text = getString(R.string.nav_compare_description)
        val btn = view.findViewById<Button>(R.id.btn)
        btn.text = getString(R.string.nav_compare_btn_1)
        btn.setOnClickListener {
            startTimestamp = System.currentTimeMillis()
            navigationScene!!.startActivity(Intent(activity, EmptyAppCompatActivity::class.java))
        }
        val btn2 = view.findViewById<Button>(R.id.btn2)
        btn2.visibility = View.VISIBLE
        btn2.text = getString(R.string.nav_compare_btn_2)
        btn2.setOnClickListener {
            startTimestamp = System.currentTimeMillis()
            navigationScene!!.push(EmptyAppCompatScene::class.java)
        }
    }

    companion object {
        var startTimestamp: Long = 0
    }
}