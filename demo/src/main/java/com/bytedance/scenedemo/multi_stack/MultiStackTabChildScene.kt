package com.bytedance.scenedemo.multi_stack

import com.bytedance.scene.group.GroupScene
import android.view.ViewGroup
import android.widget.TextView
import android.view.LayoutInflater
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.bytedance.scene.Scene
import com.bytedance.scene.ktx.navigationScene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/7/18.
 */
class MultiStackTabChildScene : GroupScene() {
    private var mRootView: ViewGroup? = null
    private lateinit var mLifecycleView: TextView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        mRootView = inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
        return mRootView!!
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val index = if (arguments == null) 0 else arguments!!.getInt("index")
        mRootView!!.setBackgroundColor(ColorUtil.getMaterialColor(resources, index))
        mLifecycleView = view.findViewById(R.id.name)
        mLifecycleView.text = stateHistory
        val btn = view.findViewById<Button>(R.id.btn)
        btn.setText(R.string.nav_multi_stack_sub_btn_1)
        btn.setOnClickListener { mLifecycleView.setText("") }
        val btn2 = view.findViewById<Button>(R.id.btn2)
        btn2.visibility = View.VISIBLE
        btn2.setText(R.string.nav_multi_stack_sub_btn_2)
        btn2.setOnClickListener { navigationScene!!.push(EmptyScene::class.java) }
        val btn3 = view.findViewById<Button>(R.id.btn3)
        btn3.visibility = View.VISIBLE
        btn3.setText(R.string.nav_multi_stack_sub_btn_3)
        btn3.setOnClickListener { parentScene!!.navigationScene!!.push(EmptyScene::class.java) }
    }

    override fun onResume() {
        super.onResume()
        mLifecycleView!!.text = stateHistory
    }

    class EmptyScene : Scene() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            val view = View(activity)
            view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 3))
            return view
        }
    }
}