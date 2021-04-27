package com.bytedance.scenedemo.navigation.push_singletop

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.bytedance.scenedemo.R
import android.widget.TextView
import com.bytedance.scene.Scene
import com.bytedance.scene.ktx.navigationScene
import com.bytedance.scenedemo.navigation.push_singletop.PushSingleTopScene_1
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/2/18.
 */
class PushSingleTopScene_0 : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.basic_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity != null) {
            view.setBackgroundColor(ColorUtil.getMaterialColor(activity!!.resources, 1))
        }
        val name = view.findViewById<TextView>(R.id.name)
        name.text = navigationScene!!.stackHistory
        val btn = view.findViewById<Button>(R.id.btn)
        btn.text = getString(R.string.nav_single_top_btn_0)
        btn.setOnClickListener { navigationScene!!.push(PushSingleTopScene_1::class.java) }
    }
}