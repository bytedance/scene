package com.bytedance.scenedemo.architecture_patterns.scope

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bytedance.scene.Scene
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.utlity.ViewIdGenerator
import com.bytedance.scenedemo.utility.ColorUtil


class ScopeSample : GroupScene() {
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
        scope.register("parent_liveData", mLiveData)
        mLiveData.observe(this, Observer {
            mTextView.text = "Counter $it"
        })
        add(mId, ScopeChildScene(), "TAG")
    }
}

class ScopeChildScene : Scene() {
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
        val liveData: MutableLiveData<Int> = scope.getService<MutableLiveData<Int>>("parent_liveData")!!
        mButton.setOnClickListener {
            liveData.value = (liveData.value ?: 0) + 1
        }
    }
}