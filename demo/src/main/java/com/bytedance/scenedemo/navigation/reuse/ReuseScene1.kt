package com.bytedance.scenedemo.navigation.reuse

import android.graphics.Color
import com.bytedance.scene.group.ReuseGroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.os.SystemClock
import android.widget.FrameLayout

/**
 * Created by JiangQi on 8/13/18.
 */
class ReuseScene1 : ReuseGroupScene() {
    override fun onCreateNewView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): ViewGroup {
        SystemClock.sleep(1000)
        val view: ViewGroup = FrameLayout(activity)
        view.setBackgroundColor(Color.YELLOW)
        return view
    }
}