package com.bytedance.scenedemo.group.async_inflate

import com.bytedance.scene.group.GroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.widget.FrameLayout
import com.bytedance.scenedemo.group.async_inflate.AsyncInflateSceneDemo.TestAsyncInflateScene
import com.bytedance.scene.group.AsyncLayoutGroupScene
import com.bytedance.scenedemo.R
import android.graphics.drawable.AnimatedVectorDrawable
import android.view.View
import android.widget.ImageView

/**
 * Created by JiangQi on 9/19/18.
 */
class AsyncInflateSceneDemo : GroupScene() {
    private var id = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        id = View.generateViewId()
        val frameLayout = FrameLayout(requireActivity())
        frameLayout.id = id
        return frameLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val scene = TestAsyncInflateScene()
        scene.isAsyncLayoutEnabled = true
        add(id, scene, "wo")
    }

    class TestAsyncInflateScene : AsyncLayoutGroupScene() {
        override fun getLayoutId(): Int {
            return R.layout.layout_test_async_layout
        }

        override fun onAsyncActivityCreated(savedInstanceState: Bundle?) {
            super.onAsyncActivityCreated(savedInstanceState)
            ((findViewById<View>(R.id.imageview) as ImageView?)!!.drawable as AnimatedVectorDrawable).start()
        }
    }
}