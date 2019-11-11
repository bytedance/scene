package com.bytedance.scenedemo.architecture_patterns.by_constructor

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.bytedance.scene.Scene
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.utlity.ViewIdGenerator
import com.bytedance.scenedemo.utility.ColorUtil


class ByConstructorSample : GroupScene() {
    private val mLiveData: MutableLiveData<Int> = MutableLiveData()
    private lateinit var mTextView: TextView
    private var mId: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        val layout = LinearLayout(requireSceneContext()).apply {
            setBackgroundColor(ColorUtil.getMaterialColor(resources, 0))
            orientation = LinearLayout.VERTICAL
        }
        mTextView = TextView(requireSceneContext()).apply {
            text = "Counter 0"
            gravity = Gravity.CENTER
        }
        layout.addView(mTextView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300))

        val sceneContainer = FrameLayout(requireSceneContext())
        mId = ViewIdGenerator.generateViewId()
        sceneContainer.id = mId
        layout.addView(sceneContainer, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300))
        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mLiveData.observe(this, Observer {
            mTextView.text = "Counter $it"
        })
        disableSupportRestore()
        add(mId, ByConstructorSampleChildScene(mLiveData) {
            mLiveData.value = it
        }, "TAG")
    }
}

class ByConstructorSampleChildScene(private val liveData: LiveData<Int>, private val setValue: (Int) -> Unit) : Scene() {
    private lateinit var mButton: Button
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val frameLayout = FrameLayout(requireSceneContext()).apply {
            setBackgroundColor(ColorUtil.getMaterialColor(resources, 1))
        }
        mButton = Button(requireSceneContext()).apply {
            text = "Click +1"
        }
        frameLayout.addView(mButton)
        return frameLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mButton.setOnClickListener {
            setValue((liveData.value ?: 0) + 1)
        }
    }
}