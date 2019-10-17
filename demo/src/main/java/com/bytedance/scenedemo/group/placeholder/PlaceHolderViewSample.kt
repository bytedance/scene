package com.bytedance.scenedemo.group.placeholder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bytedance.scene.group.GroupScene


class PlaceHolderViewSample : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return FrameLayout(requireSceneContext())
    }
}