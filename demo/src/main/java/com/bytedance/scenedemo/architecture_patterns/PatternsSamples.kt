package com.bytedance.scenedemo.architecture_patterns

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bytedance.scene.Scene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.architecture_patterns.mvvm.ViewModelSceneSamples


class PatternsSamples  : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val scrollView = ScrollView(activity)

        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL

        scrollView.addView(layout)

        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_basic))

        addButton(layout, getString(R.string.patterns_mvp), View.OnClickListener {
            Toast.makeText(requireApplicationContext(),"TODO",Toast.LENGTH_SHORT).show()
        })

        addButton(layout, getString(R.string.patterns_mvvm), View.OnClickListener {
            requireNavigationScene().push(ViewModelSceneSamples::class.java)
        })

        addButton(layout, getString(R.string.patterns_mvi), View.OnClickListener {
            Toast.makeText(requireApplicationContext(),"TODO",Toast.LENGTH_SHORT).show()
        })

        addButton(layout, getString(R.string.patterns_viper), View.OnClickListener {
            Toast.makeText(requireApplicationContext(),"TODO",Toast.LENGTH_SHORT).show()
        })

        addSpace(layout, 100)

        return scrollView
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