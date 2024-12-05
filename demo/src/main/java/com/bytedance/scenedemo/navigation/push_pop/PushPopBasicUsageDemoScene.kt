package com.bytedance.scenedemo.navigation.push_pop

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import com.bytedance.scene.Scene
import com.bytedance.scene.animation.animatorexecutor.DialogSceneAnimatorExecutor
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace
import com.bytedance.scenedemo.utility.addTitle

/**
 * Created by JiangQi on 7/30/18.
 */
class PushPopBasicUsageDemoScene : Scene() ,ActivityCompatibleBehavior{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
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
        addTitle(layout, getString(R.string.main_title_basic))

        addButton(layout, value.toString(), View.OnClickListener {
            val bundle = Bundle()
            bundle.putInt("1", value + 1)
            requireNavigationScene().push(PushPopBasicUsageDemoScene::class.java, bundle)
        })

        addButton(layout, "Dialog", View.OnClickListener {
            val bundle = Bundle()
            bundle.putInt("1", value + 1)
            requireNavigationScene().push(
                PushPopBasicUsageDemoTranslucentScene::class.java, bundle, PushOptions.Builder()
                    .setAnimation(DialogSceneAnimatorExecutor()).build()
            )
        })

        addSpace(layout, 100)

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

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.i("PushPopBasicUsageDemoScene","Not yet implemented")
    }

    override fun onNewIntent(arguments: Bundle?) {
        Log.i("PushPopBasicUsageDemoScene","Not yet implemented")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        Log.i("PushPopBasicUsageDemoScene","Not yet implemented")
    }
}
