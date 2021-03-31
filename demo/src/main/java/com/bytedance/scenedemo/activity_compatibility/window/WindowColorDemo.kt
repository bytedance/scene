package com.bytedance.scenedemo.activity_compatibility.window

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import com.bytedance.scene.Scene
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/21/18.
 */
class WindowColorDemo : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return View(activity)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 1))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().window.statusBarColor = ColorUtil.getMaterialColor(
                resources, 2
            )
            requireActivity().window.navigationBarColor = ColorUtil.getMaterialColor(
                resources, 3
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requireActivity().window.decorView.systemUiVisibility =
                (requireActivity().window.decorView.systemUiVisibility
                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().window.decorView.systemUiVisibility =
                (requireActivity().window.decorView.systemUiVisibility
                        or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        }
    }
}