package com.bytedance.scenedemo.group.basic_usage

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.bytedance.scene.Scene
import com.bytedance.scene.ui.template.AppCompatScene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/7/18.
 */
class GroupSceneBasicUsageSample : AppCompatScene() {

    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.group_scene_page_block, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTitle(R.string.main_part_btn_child_scene)

        val scene = createOrReuse("0") { BasicUsageScene.newInstance(0) }
        val scene1 = createOrReuse("1") { BasicUsageScene.newInstance(1) }
        val scene2 = createOrReuse("2") { BasicUsageScene.newInstance(2) }
        val scene3 = createOrReuse("3") { BasicUsageScene.newInstance(3) }

        if (!isAdded(scene))
            add(R.id.block_0, scene, "0")
        if (!isAdded(scene1))
            add(R.id.block_1, scene1, "1")
        if (!isAdded(scene2))
            add(R.id.block_2, scene2, "2")
        if (!isAdded(scene3))
            add(R.id.block_3, scene3, "3")

        requireViewById<View>(R.id.btn_0).setOnClickListener {
            if (isAdded(scene)) {
                if (isShow(scene)) {
                    hide(scene)
                } else {
                    show(scene)
                }
            } else {
                add(R.id.block_0, scene, "0")
            }
        }

        requireViewById<View>(R.id.btn_1).setOnClickListener {
            if (isAdded(scene)) {
                remove(scene)
            } else {
                add(R.id.block_0, scene, "0")
            }
        }

        requireViewById<View>(R.id.btn_2).setOnClickListener {
            if (isAdded(scene)) {
                if (isShow(scene)) {
                    hide(scene, android.R.anim.slide_out_right)
                } else {
                    show(scene, android.R.anim.slide_in_left)
                }
            } else {
                add(R.id.block_0, scene, "0", R.anim.slide_in_from_right)
            }
        }

        requireViewById<View>(R.id.btn_3).setOnClickListener {
            if (isAdded(scene)) {
                remove(scene, R.anim.slide_out_to_left)
            } else {
                add(R.id.block_0, scene, "0", R.anim.slide_in_from_right)
            }
        }
    }

    class BasicUsageScene : Scene() {

        private lateinit var name: TextView

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            val layout = FrameLayout(activity!!)
            name = TextView(activity)
            val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.gravity = Gravity.CENTER
            layout.addView(name, layoutParams)
            return layout
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            val index = arguments!!.getInt("index", 0)
            view.setBackgroundColor(ColorUtil.getMaterialColor(resources, index))
            name.text = "Child Scene$index"
        }

        companion object {
            fun newInstance(index: Int): BasicUsageScene {
                val scene = BasicUsageScene()
                val bundle = Bundle()
                bundle.putInt("index", index)
                scene.setArguments(bundle)
                return scene
            }
        }
    }
}
