package com.bytedance.scenedemo.navigation.recreate

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatDelegate
import com.bytedance.scene.Scene
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior
import com.bytedance.scene.ktx.activityAttributes
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace
import com.bytedance.scenedemo.utility.addTitle

/**
 * Created by jiangqi on 2024/7/4
 * @author jiangqi@bytedance.com
 */
class RecreateUsageDemoScene : Scene(),
    ActivityCompatibleBehavior {

    var isNightMode = false

    init {
        activityAttributes {
            configChanges = ActivityInfo.CONFIG_UI_MODE
        }
    }

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
        addTitle(layout, resources.getString(R.string.night_mode_status))

        addButton(layout, value.toString(), View.OnClickListener {
            val bundle = Bundle()
            bundle.putInt("1", value + 1)
            requireNavigationScene().push(RecreateUsageDemoScene::class.java, bundle)
        })

        addButton(layout, "Recreate current Scene", View.OnClickListener {
            requireNavigationScene().recreate(this)
        })

        addButton(layout, "Switch to NightMode or not", View.OnClickListener {
            AppCompatDelegate.setDefaultNightMode(
                if (isNightMode) {
                    isNightMode = false
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    isNightMode = true
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            )
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.i("onConfigurationChanged", this.toString() + " $newConfig")
    }

    override fun onNewIntent(arguments: Bundle?) {

    }
}