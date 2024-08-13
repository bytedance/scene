package com.bytedance.scenedemo.navigation.singletask

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scene.launchmode.LaunchMode
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 9/4/18.
 */
class ClickToOpenSingleTaskScene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.layout_click_to_open_single_task, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 1))
        val name = view.findViewById<TextView>(R.id.name)
        name.text = requireNavigationScene().stackHistory

        val checkBox = view.findViewById<CheckBox>(R.id.use_post)

        val btn = view.findViewById<Button>(R.id.btn)
        btn.text = getString(R.string.nav_single_task_btn_1)
        btn.setOnClickListener {
            val options = PushOptions.Builder()
                .setLaunchMode(LaunchMode.SINGLE_TASK)
                .setUsePost(checkBox.isChecked)
                .build()
            requireNavigationScene().push(SingleTaskDemoScene::class.java, null, options)
        }
    }
}