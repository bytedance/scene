package com.bytedance.scenedemo.extreme_case

import com.bytedance.scene.group.GroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.os.Handler
import com.bytedance.scenedemo.R
import android.widget.TextView
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.interfaces.PopOptions
import com.bytedance.scene.interfaces.PopOptions.CountUtilPredicate
import android.widget.Toast
import android.os.Looper
import android.view.View
import android.widget.Button
import com.bytedance.scenedemo.lifecycle.EmptyScene
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 9/5/18.
 */
class Case2Scene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 0))
        val name = view.findViewById<TextView>(R.id.name)
        name.visibility = View.GONE
        val btn = view.findViewById<Button>(R.id.btn)
        btn.setText(R.string.case_pop_many_btn_1)
        btn.setOnClickListener {
            navigationScene!!.push(EmptyScene::class.java, null, PushOptions.Builder().build())
            for (i in 0..99) {
                navigationScene!!.pop()
            }
        }
        val btn2 = view.findViewById<Button>(R.id.btn2)
        btn2.visibility = View.VISIBLE
        btn2.setText(R.string.case_pop_many_btn_2)
        btn2.setOnClickListener {
            navigationScene!!.push(EmptyScene::class.java, null, PushOptions.Builder().build())
            navigationScene!!.pop(PopOptions.Builder().setPopUtilPredicate(CountUtilPredicate(100)).build())
        }
        val btn3 = view.findViewById<Button>(R.id.btn3)
        btn3.visibility = View.VISIBLE
        btn3.setText(R.string.case_pop_many_btn_3)
        btn3.setOnClickListener {
            Toast.makeText(activity, R.string.case_pop_many_toast_1, Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                for (i in 0..99) {
                    navigationScene!!.pop()
                }
                navigationScene!!.push(EmptyScene::class.java, null, PushOptions.Builder().build())
                Toast.makeText(activity, R.string.case_pop_many_toast_2, Toast.LENGTH_SHORT).show()
            }, 3000)
        }
    }
}