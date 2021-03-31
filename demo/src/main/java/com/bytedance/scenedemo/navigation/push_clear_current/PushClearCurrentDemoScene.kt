package com.bytedance.scenedemo.navigation.push_clear_current

import com.bytedance.scene.group.GroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.widget.Button
import com.bytedance.scenedemo.R
import android.widget.TextView
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/17/18.
 */
class PushClearCurrentDemoScene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 0))
        val name = view.findViewById<TextView>(R.id.name)
        name.text = navigationScene!!.stackHistory
        val btn = view.findViewById<Button>(R.id.btn)
        btn.text = getString(R.string.nav_clear_current_btn)
        btn.setOnClickListener {
            navigationScene!!.push(
                EmptyScene::class.java,
                null,
                PushOptions.Builder().clearCurrent().build()
            )
        }
    }
}