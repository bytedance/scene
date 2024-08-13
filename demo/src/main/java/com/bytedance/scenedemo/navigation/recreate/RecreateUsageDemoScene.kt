package com.bytedance.scenedemo.navigation.recreate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import com.bytedance.scene.Scene
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scenedemo.utility.ColorUtil
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace
import com.bytedance.scenedemo.utility.addTitle

/**
 * Created by jiangqi on 2024/7/4
 * @author jiangqi@bytedance.com
 */
class RecreateUsageDemoScene : Scene(){

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?
    ): View {
        val scrollView = ScrollView(requireSceneContext())
        scrollView.fitsSystemWindows = true

        val layout = LinearLayout(requireSceneContext())
        layout.orientation = LinearLayout.VERTICAL

        scrollView.addView(layout)

        val argument = arguments
        val value = argument?.getInt("1", 0) ?: 0
        scrollView.setBackgroundColor(ColorUtil.getMaterialColor(activity!!.resources, value))

        addClassPathTitle(layout)
        addSpace(layout, 12)
        addTitle(layout, "Current scene instance: ${this.toString()}")

        addButton(layout, value.toString(), View.OnClickListener {
            val bundle = Bundle()
            bundle.putInt("1", value + 1)
            requireNavigationScene().push(RecreateUsageDemoScene::class.java, bundle)
        })

        addButton(layout, "Recreate current Scene", View.OnClickListener {
            requireNavigationScene().recreate(this)
        })

        addSpace(layout, 100)

        return scrollView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val argument = arguments
        val value = argument?.getInt("1", 0) ?: 0

        Log.i("Lifecycle", this.toString() + " $value onActivityCreated")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val argument = arguments
        val value = argument?.getInt("1", 0) ?: 0

        Log.i("Lifecycle", this.toString() + " $value onDestroyView")
    }
}