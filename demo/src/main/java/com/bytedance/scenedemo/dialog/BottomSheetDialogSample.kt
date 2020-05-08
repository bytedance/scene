package com.bytedance.scenedemo.dialog

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.bytedance.scene.dialog.BottomSheetDialogScene

/**
 * Created by JiangQi on 11/25/18.
 */
class BottomSheetDialogSample : BottomSheetDialogScene() {
    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val view = LinearLayout(activity)
        view.orientation = LinearLayout.VERTICAL
        val first = TextView(activity)
        first.setBackgroundColor(Color.BLUE)
        first.text = "Expand Me!"
        first.setTextColor(Color.WHITE)
        first.gravity = Gravity.CENTER_VERTICAL
        first.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
        view.addView(first, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400))
        val second = View(activity)
        second.setBackgroundColor(Color.YELLOW)
        view.addView(second, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1600))
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        behavior.peekHeight = 400
    }
}