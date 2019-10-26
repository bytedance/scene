package com.bytedance.scenedemo.group.placeholder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bytedance.scene.group.GroupScene
import com.bytedance.scenedemo.R


class PlaceHolderViewSample : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        disableSupportRestore()
        return inflater.inflate(R.layout.layout_placeholderviewsample, container, false) as ViewGroup
    }
}