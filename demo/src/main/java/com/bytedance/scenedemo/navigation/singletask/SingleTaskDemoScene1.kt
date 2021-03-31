package com.bytedance.scenedemo.navigation.singletask

import com.bytedance.scene.group.GroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.widget.Button
import com.bytedance.scenedemo.R
import android.widget.TextView
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.interfaces.PushOptions.SingleTaskPredicate
import com.bytedance.scenedemo.navigation.singletask.SingleTaskDemoScene
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 9/4/18.
 */
class SingleTaskDemoScene1 : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 1))
        val name = view.findViewById<TextView>(R.id.name)
        name.text = requireNavigationScene().stackHistory
        val btn = view.findViewById<Button>(R.id.btn)
        btn.text = getString(R.string.nav_single_task_btn_1)
        btn.setOnClickListener {
            val options = PushOptions.Builder()
                .setRemovePredicate(SingleTaskPredicate(SingleTaskDemoScene::class.java))
                .build()
            requireNavigationScene().push(SingleTaskDemoScene::class.java, null, options)
        }
    }
}