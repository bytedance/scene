package com.bytedance.scenedemo.navigation.push_pop_with_post

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.widget.LinearLayout
import android.widget.ScrollView
import com.bytedance.scene.Scene
import com.bytedance.scene.animation.animatorexecutor.DialogSceneAnimatorExecutor
import com.bytedance.scene.interfaces.PopOptions
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.ktx.post
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
class PushPopWithPostBasicUsageDemoScene : Scene() {
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
            requireNavigationScene().push(PushPopWithPostBasicUsageDemoScene::class.java, bundle,PushOptions.Builder().setUsePost(true).build())
        })

        addButton(layout, "Dialog", View.OnClickListener {
            val bundle = Bundle()
            bundle.putInt("1", value + 1)
            requireNavigationScene().push(
                PushPopWithPostBasicUsageDemoTranslucentScene::class.java, bundle, PushOptions.Builder()
                    .setAnimation(DialogSceneAnimatorExecutor()).build()
            )
        })

        addButton(layout, "Back", View.OnClickListener {
            requireNavigationScene().pop(PopOptions.Builder().setUsePost(true).setUseIdleWhenStop(true).build())
        })

        addSpace(layout, 100)

        return scrollView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val argument = arguments
        val value = argument?.getInt("1", 0) ?: 0

        Log.i("Lifecycle",this.toString() + " $value onActivityCreated")

        post(Runnable {
            Log.i("Lifecycle",this.toString() + " $value onActivityCreated by Post")
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val argument = arguments
        val value = argument?.getInt("1", 0) ?: 0

        Log.i("Lifecycle",this.toString() + " $value onDestroyView")
    }

    override fun onStart() {
        super.onStart()

        val argument = arguments
        val value = argument?.getInt("1", 0) ?: 0

        Log.i("Lifecycle",this.toString() + " $value onStart")
    }

    override fun onResume() {
        super.onResume()

        val argument = arguments
        val value = argument?.getInt("1", 0) ?: 0

        Log.i("Lifecycle",this.toString() + " $value onResume")
        post(Runnable {
            Log.i("Lifecycle", this.toString() + " $value onResume by Post")
        })
    }

    override fun onPause() {
        super.onPause()

        val argument = arguments
        val value = argument?.getInt("1", 0) ?: 0

        Log.i("Lifecycle",this.toString() + " $value onPause")

        post(Runnable {
            Log.i("Lifecycle",this.toString() + " $value onPause by Post")
        })
    }

    override fun onStop() {
        super.onStop()

        val argument = arguments
        val value = argument?.getInt("1", 0) ?: 0

        Log.i("Lifecycle",this.toString() + " $value onStop")
    }
}
