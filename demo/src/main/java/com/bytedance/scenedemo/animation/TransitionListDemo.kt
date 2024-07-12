package com.bytedance.scenedemo.animation

import android.os.Build
import androidx.annotation.RequiresApi
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.bytedance.scenedemo.R
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scene.ui.template.AppCompatScene
import com.bytedance.scenedemo.animation.grid.GridMainScene

/**
 * Created by JiangQi on 8/23/18.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
class TransitionListDemo : AppCompatScene() {
    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View? {
        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL
        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_basic))
        addButton(
            layout,
            getString(R.string.main_anim_btn_share_element),
            View.OnClickListener { requireNavigationScene().push(GridMainScene::class.java) })
        addSpace(layout, 100)
        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTitle(R.string.main_anim_btn_share_element)
    }

    private fun addTitle(parent: LinearLayout, text: String) {
        val textView = TextView(activity)
        textView.textSize = 14f
        textView.text = text
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.leftMargin = 30
        lp.rightMargin = 30
        lp.topMargin = 24
        lp.bottomMargin = 24
        parent.addView(textView, lp)
    }

    private fun addButton(
        parent: LinearLayout,
        text: String,
        onClickListener: View.OnClickListener
    ): Button {
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