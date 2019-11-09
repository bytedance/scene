package com.bytedance.scenedemo.navigation.forresult

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import com.bytedance.scene.Scene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/3/18.
 */
class SceneResultScene_1 : Scene() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 2))

        val name = view.findViewById<TextView>(R.id.name)
        name.text = navigationScene!!.stackHistory

        val editText = view.findViewById<EditText>(R.id.edit_text)
        editText.visibility = View.VISIBLE
        editText.setText(1234.toString())

        val btn = view.findViewById<Button>(R.id.btn)
        btn.text = getString(R.string.nav_result_set_result_btn)
        btn.setOnClickListener { requireNavigationScene().setResult(this@SceneResultScene_1, editText.text.toString()) }
    }
}
