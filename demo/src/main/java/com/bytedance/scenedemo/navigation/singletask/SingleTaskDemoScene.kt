package com.bytedance.scenedemo.navigation.singletask

import com.bytedance.scene.group.GroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.widget.Button
import com.bytedance.scenedemo.R
import android.widget.TextView
import com.bytedance.scenedemo.navigation.singletask.SingleTaskDemoScene1
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 9/4/18.
 */
class SingleTaskDemoScene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 0))
        val name = view.findViewById<TextView>(R.id.name)
        name.text = requireNavigationScene().stackHistory
        val btn = view.findViewById<Button>(R.id.btn)
        btn.text = getString(R.string.nav_single_task_btn_0)
        btn.setOnClickListener { requireNavigationScene().push(SingleTaskDemoScene1::class.java) }
    }
}