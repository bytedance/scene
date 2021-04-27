package com.bytedance.scenedemo.animation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bytedance.scene.Scene
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil
import java.util.*

/**
 * Created by JiangQi on 8/15/18.
 */
class AnimationResDemoScene : GroupScene() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        val layout = LinearLayout(activity)
        layout.setBackgroundColor(ColorUtil.getMaterialColor(resources, 0))
        layout.fitsSystemWindows = true
        layout.orientation = LinearLayout.VERTICAL

        val enterAnimationRes = intArrayOf(R.anim.slide_in_from_right, R.anim.abc_fade_in, android.R.anim.fade_in, android.R.anim.slide_in_left)

        val enterAnimationResStr = ArrayList<String>()
        for (resId in enterAnimationRes) {
            enterAnimationResStr.add(requireActivity().resources.getResourceEntryName(resId))
        }

        val exitAnimationRes = intArrayOf(R.anim.slide_out_to_left, R.anim.abc_fade_out, android.R.anim.fade_out, android.R.anim.slide_out_right)

        val exitAnimationResStr = ArrayList<String>()
        for (resId in exitAnimationRes) {
            exitAnimationResStr.add(requireActivity().resources.getResourceEntryName(resId))
        }

        var lp: LinearLayout.LayoutParams

        var name = TextView(activity)
        name.setText(R.string.anim_xml_tip_1)
        lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = 24
        lp.leftMargin = 30
        lp.rightMargin = 30
        layout.addView(name, lp)

        val enterSpinner = Spinner(activity)
        lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120)
        lp.leftMargin = 20
        lp.rightMargin = 20
        layout.addView(enterSpinner, lp)

        var adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, enterAnimationResStr)
        enterSpinner.adapter = adapter

        name = TextView(activity)
        name.setText(R.string.anim_xml_tip_2)
        lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.leftMargin = 30
        lp.rightMargin = 30
        layout.addView(name, lp)

        val exitSpinner = Spinner(activity)
        lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120)
        lp.leftMargin = 20
        lp.rightMargin = 20
        layout.addView(exitSpinner, lp)
        adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, exitAnimationResStr)
        exitSpinner.adapter = adapter

        val button = Button(activity)
        button.setText(R.string.anim_xml_btn)
        lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150)
        lp.leftMargin = 20
        lp.rightMargin = 20
        layout.addView(button, lp)
        button.setOnClickListener {
            val enter = enterAnimationRes[enterAnimationResStr.indexOf(enterSpinner.selectedItem)]
            val exit = exitAnimationRes[exitAnimationResStr.indexOf(exitSpinner.selectedItem)]
            requireNavigationScene().push(EmptyScene::class.java, null,
                    PushOptions.Builder().setAnimation(requireActivity(), enter, exit).build())
        }
        return layout
    }

    class EmptyScene : Scene() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            val view = View(requireSceneContext())
            view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 1))
            return view
        }
    }
}
