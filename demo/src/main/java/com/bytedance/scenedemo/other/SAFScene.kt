package com.bytedance.scenedemo.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.Scene

/**
 * Created by jiangqi on 2024/10/30
 * @author jiangqi@bytedance.com
 */
class SAFScene : Scene() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?
    ): View {
        return View(requireSceneContext())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }
}