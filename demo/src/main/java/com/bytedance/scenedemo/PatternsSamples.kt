package com.bytedance.scenedemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.bytedance.scene.Scene
import com.bytedance.scenedemo.architecture_patterns.mvvm.ViewModelSceneSamples
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace
import com.bytedance.scenedemo.utility.addTitle


class PatternsSamples : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val scrollView = ScrollView(activity)

        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL

        scrollView.addView(layout)

        addClassPathTitle(layout)
        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_basic))

        addButton(layout, getString(R.string.patterns_scope), View.OnClickListener {
            Toast.makeText(requireApplicationContext(), "TODO", Toast.LENGTH_SHORT).show()
        })

        addButton(layout, getString(R.string.patterns_mvp), View.OnClickListener {
            Toast.makeText(requireApplicationContext(), "TODO", Toast.LENGTH_SHORT).show()
        })

        addButton(layout, getString(R.string.patterns_mvp), View.OnClickListener {
            Toast.makeText(requireApplicationContext(), "TODO", Toast.LENGTH_SHORT).show()
        })

        addButton(layout, getString(R.string.patterns_mvvm), View.OnClickListener {
            requireNavigationScene().push(ViewModelSceneSamples::class.java)
        })

        addButton(layout, getString(R.string.patterns_mvi), View.OnClickListener {
            Toast.makeText(requireApplicationContext(), "TODO", Toast.LENGTH_SHORT).show()
        })

        addButton(layout, getString(R.string.patterns_viper), View.OnClickListener {
            Toast.makeText(requireApplicationContext(), "TODO", Toast.LENGTH_SHORT).show()
        })

        addSpace(layout, 100)

        return scrollView
    }
}