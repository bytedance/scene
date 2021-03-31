package com.bytedance.scenedemo.lifecycle

import com.bytedance.scene.group.GroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.bytedance.scene.Scene
import com.bytedance.scenedemo.R
import com.bytedance.scene.interfaces.ChildSceneLifecycleCallbacks
import com.bytedance.scenedemo.lifecycle.ChildSceneLifecycleCallbacksDemoScene
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 9/6/18.
 */
class ChildSceneLifecycleCallbacksDemoScene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL
        val textView = TextView(activity)
        textView.setText(R.string.lifecycle_callback_tip)
        layout.addView(textView)
        textView.setOnClickListener { navigationScene!!.push(EmptyScene0::class.java) }
        layout.setBackgroundColor(ColorUtil.getMaterialColor(resources, 1))
        layout.fitsSystemWindows = true
        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navigationScene!!.registerChildSceneLifecycleCallbacks(mChildSceneLifecycleCallbacks, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        navigationScene!!.unregisterChildSceneLifecycleCallbacks(mChildSceneLifecycleCallbacks)
    }

    class EmptyScene0 : Scene() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            val layout = LinearLayout(activity)
            layout.orientation = LinearLayout.VERTICAL
            val textView = TextView(activity)
            textView.text = navigationScene!!.stackHistory
            layout.addView(textView)
            textView.setOnClickListener { navigationScene!!.push(EmptyScene1::class.java) }
            layout.setBackgroundColor(ColorUtil.getMaterialColor(resources, 1))
            layout.fitsSystemWindows = true
            return layout
        }
    }

    class EmptyScene1 : Scene() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            val layout = LinearLayout(activity)
            layout.orientation = LinearLayout.VERTICAL
            val textView = TextView(activity)
            textView.text = navigationScene!!.stackHistory
            layout.addView(textView)
            textView.setOnClickListener { }
            layout.setBackgroundColor(ColorUtil.getMaterialColor(resources, 1))
            layout.fitsSystemWindows = true
            return layout
        }
    }

    private val mChildSceneLifecycleCallbacks: ChildSceneLifecycleCallbacks = object : ChildSceneLifecycleCallbacks {
        override fun onSceneCreated(scene: Scene, savedInstanceState: Bundle?) {
            log("Scene", "$scene onSceneCreated")
        }

        override fun onSceneViewCreated(scene: Scene, savedInstanceState: Bundle?) {
            log("Scene", "$scene onSceneViewCreated")
        }

        override fun onSceneActivityCreated(scene: Scene, savedInstanceState: Bundle?) {
            log("Scene", "$scene onSceneActivityCreated")
        }

        override fun onSceneStarted(scene: Scene) {
            log("Scene", "$scene onSceneStarted")
        }

        override fun onSceneResumed(scene: Scene) {
            log("Scene", "$scene onSceneResumed")
        }

        override fun onSceneSaveInstanceState(scene: Scene, outState: Bundle) {
            log("Scene", "$scene onSceneSaveInstanceState")
        }

        override fun onScenePaused(scene: Scene) {
            log("Scene", "$scene onScenePaused")
        }

        override fun onSceneStopped(scene: Scene) {
            log("Scene", "$scene onSceneStopped")
        }

        override fun onSceneViewDestroyed(scene: Scene) {
            log("Scene", "$scene onSceneViewDestroyed")
        }

        override fun onSceneDestroyed(scene: Scene) {
            log("Scene", "$scene onSceneDestroyed")
        }
    }

    companion object {
        private fun log(name: String, tag: String) {
            Log.e(name, tag)
        }
    }
}