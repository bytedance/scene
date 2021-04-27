package com.bytedance.scenedemo.extreme_case

import com.bytedance.scene.group.GroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.bytedance.scenedemo.R
import android.widget.TextView
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.animation.animatorexecutor.HorizontalTransitionAnimatorExecutor
import android.widget.LinearLayout
import com.bytedance.scene.Scene
import com.bytedance.scene.ktx.navigationScene
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 9/5/18.
 */
class Case0Scene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 0))
        val name = view.findViewById<TextView>(R.id.name)
        name.visibility = View.GONE
        val btn = view.findViewById<Button>(R.id.btn)
        btn.setText(R.string.case_push_pop_btn_1)
        btn.setOnClickListener {
            for (i in 0..99) {
                navigationScene!!.push(
                    EmptyScene::class.java, null, PushOptions.Builder()
                        .setAnimation(HorizontalTransitionAnimatorExecutor()).build()
                )
                navigationScene!!.pop()
            }
        }
        val btn2 = view.findViewById<Button>(R.id.btn2)
        btn2.visibility = View.VISIBLE
        btn2.setText(R.string.case_push_pop_btn_2)
        btn2.setOnClickListener {
            for (i in 0..99) {
                navigationScene!!.push(
                    EmptyScene::class.java, null, PushOptions.Builder()
                        .setAnimation(HorizontalTransitionAnimatorExecutor()).build()
                )
            }
            for (i in 0..99) {
                navigationScene!!.pop()
            }
        }
        val btn3 = view.findViewById<Button>(R.id.btn3)
        btn3.visibility = View.VISIBLE
        btn3.setText(R.string.case_push_pop_btn_3)
        btn3.setOnClickListener {
            navigationScene!!.push(
                EmptyScene0::class.java, null, PushOptions.Builder()
                    .setAnimation(HorizontalTransitionAnimatorExecutor()).build()
            )
            for (i in 0..98) {
                navigationScene!!.push(
                    EmptyScene::class.java, null, PushOptions.Builder()
                        .setAnimation(HorizontalTransitionAnimatorExecutor()).build()
                )
            }
            for (i in 0..98) {
                navigationScene!!.pop()
            }
        }
    }

    class EmptyScene : Scene() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            val layout = LinearLayout(activity)
            layout.orientation = LinearLayout.VERTICAL
            val textView = TextView(activity)
            textView.text = navigationScene!!.stackHistory
            layout.addView(textView)
            layout.setBackgroundColor(ColorUtil.getMaterialColor(resources, 1))
            layout.fitsSystemWindows = true
            return layout
        }
    }

    class EmptyScene0 : Scene() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            val layout = LinearLayout(activity)
            layout.orientation = LinearLayout.VERTICAL
            val textView = TextView(activity)
            textView.text = navigationScene!!.stackHistory
            layout.addView(textView)
            layout.setBackgroundColor(ColorUtil.getMaterialColor(resources, 2))
            layout.fitsSystemWindows = true
            return layout
        }
    }
}