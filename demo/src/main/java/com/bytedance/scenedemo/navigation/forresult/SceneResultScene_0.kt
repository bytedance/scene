package com.bytedance.scenedemo.navigation.forresult

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.bytedance.scene.Scene
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/3/18.
 */
class SceneResultScene_0 : Scene() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 1))

        val name = view.findViewById<TextView>(R.id.name)
        name.text = navigationScene!!.stackHistory

        val btn = view.findViewById<Button>(R.id.btn)
        btn.text = getString(R.string.nav_result_scene_to_scene_btn_0)
        btn.setOnClickListener {
            requireNavigationScene().push(SceneResultScene_1::class.java, null,
                    PushOptions.Builder().setPushResultCallback { result ->
                        if (result != null) {
                            Toast.makeText(activity, result.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }.build())
        }

        val btn2 = view.findViewById<Button>(R.id.btn2)
        btn2.visibility = View.VISIBLE
        btn2.text = getString(R.string.nav_result_scene_to_scene_btn_1)
        btn2.setOnClickListener {
            requireNavigationScene().push(SceneResultScene_0::class.java, null,
                    PushOptions.Builder().clearTask().setPushResultCallback { result ->
                        if (result != null) {
                            Toast.makeText(activity, result.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }.build())
        }
    }
}
