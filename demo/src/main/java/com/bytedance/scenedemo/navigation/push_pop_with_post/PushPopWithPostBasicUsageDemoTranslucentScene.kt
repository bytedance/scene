package com.bytedance.scenedemo.navigation.push_pop_with_post

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import com.bytedance.scene.Scene
import com.bytedance.scene.animation.animatorexecutor.DialogSceneAnimatorExecutor
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scene.navigation.SceneTranslucent
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace
import com.bytedance.scenedemo.utility.addTitle

/**
 * Created by JiangQi on 7/30/18.
 */
class PushPopWithPostBasicUsageDemoTranslucentScene : Scene(), SceneTranslucent {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val scrollView = ScrollView(requireSceneContext())
        scrollView.fitsSystemWindows = true

        val layout = LinearLayout(requireSceneContext())
        layout.orientation = LinearLayout.VERTICAL

        scrollView.addView(layout)

        val argument = arguments
        val value = argument?.getInt("1", 0) ?: 0
        layout.setBackgroundColor(ColorUtil.getMaterialColor(activity!!.resources, value))

        addClassPathTitle(layout)
        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_basic))

        addButton(layout, value.toString(), View.OnClickListener {
            val bundle = Bundle()
            bundle.putInt("1", value + 1)
            requireNavigationScene().push(PushPopWithPostBasicUsageDemoScene::class.java, bundle)
        })

        addButton(layout, "Dialog", View.OnClickListener {
            val bundle = Bundle()
            bundle.putInt("1", value + 1)
            requireNavigationScene().push(
                PushPopWithPostBasicUsageDemoTranslucentScene::class.java, bundle, PushOptions.Builder()
                    .setAnimation(DialogSceneAnimatorExecutor()).build()
            )
        })

        addButton(layout, "Close ${value-2} page", View.OnClickListener {
            requireNavigationScene().remove(
                requireNavigationScene().sceneList.get(
                    requireNavigationScene().sceneList.size - 3
                )
            )
        })

        addSpace(layout, 400)

        return scrollView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val argument = arguments
        val value = argument?.getInt("1", 0) ?: 0

        Log.i("Lifecycle",this.toString() + " $value onActivityCreated")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val argument = arguments
        val value = argument?.getInt("1", 0) ?: 0

        Log.i("Lifecycle",this.toString() + " $value onDestroyView")
    }
}
