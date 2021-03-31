package com.bytedance.scenedemo.activity_compatibility.configuration

import com.bytedance.scene.group.GroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.bytedance.scenedemo.R
import android.widget.TextView
import com.bytedance.scene.navigation.ConfigurationChangedListener
import android.widget.Toast
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 9/4/18.
 */
class ConfigurationDemoScene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 0))
        val name = view.findViewById<TextView>(R.id.name)
        name.text = getString(R.string.nav_configuration_tip)
        val btn = view.findViewById<Button>(R.id.btn)
        btn.visibility = View.GONE
        requireNavigationScene().addConfigurationChangedListener(this, ConfigurationChangedListener {
            Toast.makeText(
                applicationContext, "onConfigurationChanged", Toast.LENGTH_SHORT
            ).show()
        })
    }
}