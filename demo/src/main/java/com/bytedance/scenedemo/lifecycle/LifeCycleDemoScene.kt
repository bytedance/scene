package com.bytedance.scenedemo.lifecycle

import com.bytedance.scenedemo.lifecycle.EmptyScene.Companion.newInstance
import com.bytedance.scene.group.GroupScene
import android.widget.FrameLayout
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import com.bytedance.scene.Scene

/**
 * Created by JiangQi on 8/28/18.
 */
class LifeCycleDemoScene : GroupScene() {
    lateinit var frameLayout: FrameLayout
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        frameLayout = FrameLayout(requireActivity())
        frameLayout!!.id = View.generateViewId()
        return frameLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (findSceneByTag<Scene?>("wo") == null) {
            add(frameLayout!!.id, newInstance(0), "wo")
        }
        if (findSceneByTag<Scene?>("wo") == null) {
            add(frameLayout!!.id, newInstance(0), "wo")
        }

//        beginTransaction();
//        if (findSceneByTag("wo") == null) {
//            add(frameLayout.getId(), EmptyAppCompatScene.newInstance(0), "wo");
//        }
//
//        if (findSceneByTag("wo") == null) {
//            add(frameLayout.getId(), EmptyAppCompatScene.newInstance(0), "wo");
//        }
//        commitTransaction();
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }
}