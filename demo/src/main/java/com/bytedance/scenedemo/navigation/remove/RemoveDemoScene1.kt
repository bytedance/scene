package com.bytedance.scenedemo.navigation.remove

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.ktx.navigationScene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 9/4/18.
 */
class RemoveDemoScene1 : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 1))
        val name = view.findViewById<TextView>(R.id.name)
        name.text = navigationScene!!.stackHistory
        val btn = view.findViewById<Button>(R.id.btn)
        btn.text = getString(R.string.nav_remove_scene_in_task_1)
    }
}