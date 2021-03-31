package com.bytedance.scenedemo.extreme_case

import android.graphics.Color
import com.bytedance.scene.group.GroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.*
import com.bytedance.scenedemo.R
import com.bytedance.scene.Scene
import com.bytedance.scene.interfaces.ChildSceneLifecycleAdapterCallbacks
import com.bytedance.scenedemo.utility.ColorUtil
import java.lang.StringBuilder

/**
 * Created by JiangQi on 9/5/18.
 */
class Case5Scene : GroupScene() {
    private var mTextView: TextView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL
        val frameLayout = FrameLayout(requireActivity())
        frameLayout.id = View.generateViewId()
        layout.addView(frameLayout, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500))
        var button = Button(activity)
        button.setText(R.string.case_group_scene_btn_1)
        button.isAllCaps = false
        var lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150)
        lp.leftMargin = 20
        lp.rightMargin = 20
        layout.addView(button, lp)
        button.setOnClickListener {
            val scene = findSceneByTag<Scene>("11")
            if (scene != null) {
                remove(scene)
            } else {
                val testGroupScene = TestGroupScene()
                testGroupScene.add(TestGroupScene.ID_0, EmptyScene1(), "0")
                testGroupScene.add(TestGroupScene.ID_1, EmptyScene2(), "1")
                add(frameLayout.id, testGroupScene, "11")
            }
        }
        val testGroupScene = TestGroupScene()
        testGroupScene.add(TestGroupScene.ID_0, EmptyScene1(), "0")
        testGroupScene.add(TestGroupScene.ID_1, EmptyScene2(), "1")
        button = Button(activity)
        button.setText(R.string.case_group_scene_btn_2)
        button.isAllCaps = false
        lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150)
        lp.leftMargin = 20
        lp.rightMargin = 20
        layout.addView(button, lp)
        button.setOnClickListener {
            val scene = findSceneByTag<Scene>("11")
            if (scene != null) {
                remove(scene)
            } else {
                add(frameLayout.id, testGroupScene, "11")
            }
        }
        layout.setBackgroundColor(ColorUtil.getMaterialColor(resources, 0))
        layout.fitsSystemWindows = true
        mTextView = TextView(requireActivity())
        val scrollView = ScrollView(requireActivity())
        lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150)
        lp.leftMargin = 30
        lp.rightMargin = 30
        scrollView.addView(mTextView, lp)
        layout.addView(
            scrollView,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        navigationScene!!.registerChildSceneLifecycleCallbacks(mChildSceneLifecycleAdapterCallbacks, true)
        return layout
    }

    private val stringBuilder = StringBuilder()
    private fun ddd() {
        mTextView!!.text = stringBuilder.toString()
    }

    private val mChildSceneLifecycleAdapterCallbacks: ChildSceneLifecycleAdapterCallbacks =
        object : ChildSceneLifecycleAdapterCallbacks() {
            override fun onSceneCreated(scene: Scene, savedInstanceState: Bundle?) {
                super.onSceneCreated(scene, savedInstanceState)
                stringBuilder.append("$scene onCreated")
                stringBuilder.append("\n")
                ddd()
            }

            override fun onSceneStarted(scene: Scene) {
                super.onSceneStarted(scene)
                stringBuilder.append("$scene onStart")
                stringBuilder.append("\n")
                ddd()
            }

            override fun onSceneResumed(scene: Scene) {
                super.onSceneResumed(scene)
                stringBuilder.append("$scene onResume")
                stringBuilder.append("\n")
                ddd()
            }

            override fun onSceneSaveInstanceState(scene: Scene, outState: Bundle) {
                super.onSceneSaveInstanceState(scene, outState)
                stringBuilder.append("$scene onSaveInstanceState")
                stringBuilder.append("\n")
                ddd()
            }

            override fun onScenePaused(scene: Scene) {
                super.onScenePaused(scene)
                stringBuilder.append("$scene onPause")
                stringBuilder.append("\n")
                ddd()
            }

            override fun onSceneStopped(scene: Scene) {
                super.onSceneStopped(scene)
                stringBuilder.append("$scene onStop")
                stringBuilder.append("\n")
                ddd()
            }

            override fun onSceneDestroyed(scene: Scene) {
                super.onSceneDestroyed(scene)
                stringBuilder.append("$scene onDestroy")
                stringBuilder.append("\n")
                ddd()
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        navigationScene!!.unregisterChildSceneLifecycleCallbacks(mChildSceneLifecycleAdapterCallbacks)
    }

    override fun toString(): String {
        return javaClass.simpleName
    }

    class TestGroupScene : GroupScene() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup,
            savedInstanceState: Bundle?
        ): ViewGroup {
            val layout = LinearLayout(activity)
            layout.orientation = LinearLayout.VERTICAL
            layout.setBackgroundColor(Color.BLACK)
            val frameLayout0 = FrameLayout(requireActivity())
            frameLayout0.id = ID_0
            layout.addView(frameLayout0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100))
            val frameLayout1 = FrameLayout(requireActivity())
            frameLayout1.id = ID_1
            layout.addView(frameLayout1, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100))
            layout.fitsSystemWindows = true
            return layout
        }

        override fun toString(): String {
            return javaClass.simpleName
        }

        companion object {
            val ID_0 = View.generateViewId()
            val ID_1 = View.generateViewId()
        }
    }

    class EmptyScene1 : Scene() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            val layout = LinearLayout(activity)
            layout.orientation = LinearLayout.VERTICAL
            layout.setBackgroundColor(ColorUtil.getMaterialColor(resources, 2))
            layout.fitsSystemWindows = true
            return layout
        }

        override fun toString(): String {
            return javaClass.simpleName
        }
    }

    class EmptyScene2 : Scene() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            val layout = LinearLayout(activity)
            layout.orientation = LinearLayout.VERTICAL
            layout.setBackgroundColor(ColorUtil.getMaterialColor(resources, 3))
            layout.fitsSystemWindows = true
            return layout
        }

        override fun toString(): String {
            return javaClass.simpleName
        }
    }
}