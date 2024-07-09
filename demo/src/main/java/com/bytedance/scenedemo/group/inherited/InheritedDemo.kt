package com.bytedance.scenedemo.group.inherited

import com.bytedance.scene.group.InheritedScene
import android.widget.TextView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import com.bytedance.scene.Scene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.group.inherited.InheritedDemo.Child0Scene
import com.bytedance.scenedemo.group.inherited.InheritedDemo
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/21/18.
 */
@Deprecated("unused")
class InheritedDemo : InheritedScene() {
    lateinit var summary: TextView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.layout_inherited, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        summary = getView().findViewById(R.id.summary)
        summary.setText(R.string.part_inherited_tip)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val scene = createOrReuse("0") { Child0Scene.newInstance(0) }
        val scene1 = createOrReuse("1") { Child0Scene.newInstance(1) }
        val scene2 = createOrReuse("2") { Child0Scene.newInstance(2) }
        val scene3 = createOrReuse("3") { Child0Scene.newInstance(3) }
        if (!isAdded(scene)) add(R.id.block_0, scene, "0")
        if (!isAdded(scene1)) add(R.id.block_1, scene1, "1")
        if (!isAdded(scene2)) add(R.id.block_2, scene2, "2")
        if (!isAdded(scene3)) add(R.id.block_3, scene3, "3")
    }

    class Child0Scene : Scene() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            return View(activity)
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            val index = arguments!!.getInt("index", 0)
            view.setBackgroundColor(ColorUtil.getMaterialColor(resources, index))
            view.setOnClickListener {
                val inheritedScene: InheritedDemo? = scope.getService(InheritedDemo::class.java)
                inheritedScene?.summary?.text = "Child Scene$index"
            }
        }

        companion object {
            fun newInstance(index: Int): Child0Scene {
                val scene = Child0Scene()
                val bundle = Bundle()
                bundle.putInt("index", index)
                scene.setArguments(bundle)
                return scene
            }
        }
    }
}