package com.bytedance.scenedemo.auto_recycle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace

class ThirdScene : BaseTraceScene() {

    private val objects = mutableListOf<Any>()

    override val name: String
        get() = "ThirdScene"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val layout = LinearLayout(activity)
        layout.setBackgroundColor(resources.getColor(R.color.blue_300))
        layout.orientation = LinearLayout.VERTICAL

        addClassPathTitle(layout)

        addSpace(layout, 12)

        addButton(layout, getString(R.string.case_auto_recycle_btn4), View.OnClickListener {
            repeat(2000000) {
                objects.add(Any())
            }
            viewModel.updateMemoryUsed()
        })
        return layout
    }

}