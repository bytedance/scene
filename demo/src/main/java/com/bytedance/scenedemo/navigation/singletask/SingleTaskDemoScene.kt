package com.bytedance.scenedemo.navigation.singletask

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 9/4/18.
 */
class SingleTaskDemoScene : GroupScene(), ActivityCompatibleBehavior {
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
        btn.setOnClickListener { requireNavigationScene().push(ClickToOpenSingleTaskScene::class.java) }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {

    }

    override fun onNewIntent(arguments: Bundle?) {
        Toast.makeText(sceneContext, "onNewIntent", Toast.LENGTH_SHORT).show()
    }
}