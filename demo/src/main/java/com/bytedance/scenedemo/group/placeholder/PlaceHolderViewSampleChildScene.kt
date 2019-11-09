package com.bytedance.scenedemo.group.placeholder

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bytedance.scene.Scene
import com.bytedance.scenedemo.utility.ColorUtil


class PlaceHolderViewSampleChildScene : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return TextView(requireSceneContext()).apply {
            text = this@PlaceHolderViewSampleChildScene.javaClass.name
            gravity = Gravity.CENTER
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 0))
    }
}

class PlaceHolderViewSampleChildScene1 : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return TextView(requireSceneContext()).apply {
            text = this@PlaceHolderViewSampleChildScene1.javaClass.name
            gravity = Gravity.CENTER
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 1))
    }
}