package com.bytedance.scene.viewpager2;

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.bytedance.scene.Scene
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.bytedance.scene.ui.SceneActivity

class MainActivity : SceneActivity() {
    override fun supportRestore(): Boolean {
        return false
    }

    override fun getHomeSceneClass(): Class<out Scene> {
        return MainScene::class.java
    }
}

class MainScene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.activity_main, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val vp: ViewPager2 = findViewById(R.id.pager)!!
        vp.adapter = DemoCollectionAdapter(this)
    }
}

class DemoCollectionAdapter(groupScene: GroupScene) : SceneAdapter(groupScene) {
    override fun getItemCount(): Int = 100

    override fun createScene(position: Int): UserVisibleHintGroupScene {
        val scene = DemoObjectScene()
        scene.setArguments(Bundle().apply {
            putInt(ARG_OBJECT, position)
        })
        return scene
    }
}

private const val ARG_OBJECT = "object"

class DemoObjectScene : UserVisibleHintGroupScene() {
    private var value: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): ViewGroup {
        return inflater.inflate(R.layout.scene_collection_object, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        value = arguments?.getInt(ARG_OBJECT)!!
        val textView: TextView = view.findViewById(android.R.id.text1)
        textView.text = value.toString()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Log.d("DemoObjectScene $value", "setUserVisibleHint: $isVisibleToUser")
    }
}



