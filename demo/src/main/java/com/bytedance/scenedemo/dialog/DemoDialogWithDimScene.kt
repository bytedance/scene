package com.bytedance.scenedemo.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.bytedance.scene.Scene
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/9/18.
 */
class DemoDialogWithDimScene : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val layout = FrameLayout(requireSceneContext())
        layout.setBackgroundColor(Color.parseColor("#99000000"))
        val textView = TextView(activity)
        textView.text = requireNavigationScene().stackHistory
        textView.setBackgroundColor(ColorUtil.getMaterialColor(resources, 1))
        val layoutParams = FrameLayout.LayoutParams(600, 600)
        layoutParams.gravity = Gravity.CENTER
        layout.addView(textView, layoutParams)
        layout.setOnClickListener { requireNavigationScene().pop() }
        return layout
    }
}