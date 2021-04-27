package com.bytedance.scenedemo

import com.bytedance.scene.group.UserVisibleHintGroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.animation.AnimationResDemoScene
import com.bytedance.scenedemo.animation.SwipeBackDemo
import com.bytedance.scenedemo.animation.SlideBackButtonDemoScene
import com.bytedance.scenedemo.animation.TransitionDemo
import android.widget.TextView
import com.bytedance.scene.ktx.requireNavigationScene

/**
 * Created by JiangQi on 8/9/18.
 */
class AnimationListDemoScene : UserVisibleHintGroupScene() {
    @JvmField
    var mInteractionButton: Button? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL
        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_basic))
        addButton(
            layout,
            getString(R.string.main_anim_btn_res_anim),
            View.OnClickListener { requireNavigationScene().push(AnimationResDemoScene::class.java) })
        addButton(
            layout,
            getString(R.string.main_anim_btn_swipe_back),
            View.OnClickListener { requireNavigationScene().push(SwipeBackDemo::class.java) })
        addTitle(layout, getString(R.string.main_title_pro))
        mInteractionButton = addButton(
            layout,
            getString(R.string.main_anim_btn_ios_anim),
            View.OnClickListener { requireNavigationScene().push(SlideBackButtonDemoScene::class.java) })
        addButton(
            layout,
            getString(R.string.main_anim_btn_share_element),
            View.OnClickListener { requireNavigationScene().push(TransitionDemo::class.java) })
        addSpace(layout, 100)
        return layout
    }

    private fun addTitle(parent: LinearLayout, text: String) {
        val textView = TextView(activity)
        textView.textSize = 14f
        textView.text = text
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.leftMargin = 30
        lp.rightMargin = 30
        lp.topMargin = 24
        lp.bottomMargin = 24
        parent.addView(textView, lp)
    }

    private fun addButton(parent: LinearLayout, text: String, onClickListener: View.OnClickListener): Button {
        val button = Button(activity)
        button.isAllCaps = false
        button.text = text
        button.setOnClickListener(onClickListener)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150)
        lp.leftMargin = 20
        lp.rightMargin = 20
        parent.addView(button, lp)
        return button
    }

    private fun addSpace(parent: LinearLayout, height: Int) {
        val space = Space(activity)
        parent.addView(space, ViewGroup.LayoutParams.MATCH_PARENT, height)
    }
}