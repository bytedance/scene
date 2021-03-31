package com.bytedance.scenedemo.activity_compatibility.window

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.bytedance.scenedemo.R
import android.widget.TextView
import com.bytedance.scene.Scene
import com.bytedance.scenedemo.activity_compatibility.window.WindowColorDemo
import com.bytedance.scenedemo.activity_compatibility.window.WindowLayoutDemo
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/21/18.
 */
class WindowDemo : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.basic_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 0))
        val name = view.findViewById<TextView>(R.id.name)
        name.text = navigationScene!!.stackHistory
        val btn = view.findViewById<Button>(R.id.btn)
        btn.setText(R.string.nav_activity_states_btn_1)
        btn.setOnClickListener { requireNavigationScene().push(WindowColorDemo::class.java) }
        val btn2 = view.findViewById<Button>(R.id.btn2)
        btn2.visibility = View.VISIBLE
        btn2.setText(R.string.nav_activity_states_btn_2)
        btn2.setOnClickListener { requireNavigationScene().push(WindowLayoutDemo::class.java) }
    }
}