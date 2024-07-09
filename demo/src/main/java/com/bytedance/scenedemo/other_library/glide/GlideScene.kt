package com.bytedance.scenedemo.other_library.glide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bytedance.scene.Scene

/**
 * Created by JiangQi on 8/13/21.
 */
class GlideScene : Scene() {
    private lateinit var imageView: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return ImageView(requireSceneContext()).also {
            imageView = it
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireGlide()
                .load("https://upload.wikimedia.org/wikipedia/commons/thumb/8/80/Wikipedia-logo-v2.svg/1920px-Wikipedia-logo-v2.svg.png")
                .into(imageView)
    }
}