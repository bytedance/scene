package com.bytedance.scenedemo.navigation.reuse

import com.bytedance.scene.group.GroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.widget.Button
import com.bytedance.scenedemo.R
import android.widget.TextView
import com.bytedance.scenedemo.navigation.reuse.ReuseScene1
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/13/18.
 */
class ReuseDemoScene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 0))
        val name = view.findViewById<TextView>(R.id.name)
        name.setText(R.string.nav_reuse_tip)
        val btn = view.findViewById<Button>(R.id.btn)
        btn.setText(R.string.nav_reuse_btn)
        btn.setOnClickListener { navigationScene!!.push(ReuseScene1::class.java) }
    }
}