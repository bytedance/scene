package com.bytedance.scenedemo.navigation.push_singletop

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.bytedance.scene.Scene
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.ktx.navigationScene
import com.bytedance.scene.launchmode.LaunchMode
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/2/18.
 */
class PushSingleTopScene : Scene(), ActivityCompatibleBehavior {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.layout_push_single_top, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity != null) {
            view.setBackgroundColor(ColorUtil.getMaterialColor(activity!!.resources, 2))
        }
        val name = view.findViewById<TextView>(R.id.name)
        name.text = navigationScene!!.stackHistory

        val checkBox = view.findViewById<CheckBox>(R.id.use_post)

        val btn = view.findViewById<Button>(R.id.btn)
        btn.text = getString(R.string.nav_single_top_btn_1)
        btn.setOnClickListener {
            val options = PushOptions.Builder()
                .setLaunchMode(LaunchMode.SINGLE_TOP)
                .setUsePost(checkBox.isChecked).build()
            navigationScene!!.push(PushSingleTopScene::class.java, null, options)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {

    }

    override fun onNewIntent(arguments: Bundle?) {
        Toast.makeText(sceneContext, "onNewIntent", Toast.LENGTH_SHORT).show()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {

    }
}