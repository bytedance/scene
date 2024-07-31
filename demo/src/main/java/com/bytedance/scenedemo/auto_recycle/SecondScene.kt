package com.bytedance.scenedemo.auto_recycle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace

class SecondScene : BaseTraceScene() {

    override val name: String
        get() = "SecondScene"

    private val objects = mutableListOf<Any>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val layout = LinearLayout(activity)
        layout.setBackgroundColor(resources.getColor(R.color.red_300))
        layout.orientation = LinearLayout.VERTICAL

        addClassPathTitle(layout)

        addSpace(layout, 12)

        addButton(layout, getString(R.string.case_auto_recycle_btn3), View.OnClickListener {
            requireNavigationScene().push(ThirdScene::class.java)
        })

        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        repeat(2000000) {
            objects.add(Any())
        }

        viewModel.updateMemoryUsed()
    }
}