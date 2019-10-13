package com.bytedance.scenedemo.architecture_patterns.mvvm

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.bytedance.scene.Scene
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.ktx.activityViewModels


class ViewModelSceneSamples : GroupScene() {
    private val viewModel: SampleViewModel by activityViewModels()
    private lateinit var textView: TextView
    private lateinit var childSceneContainer: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        val layout = FrameLayout(requireSceneContext())
        textView = TextView(requireSceneContext())
        textView.gravity = Gravity.CENTER
        layout.addView(textView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300))

        childSceneContainer = FrameLayout(requireSceneContext())
        val childSceneContainerParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300)
        childSceneContainerParams.topMargin = 500
        layout.addView(childSceneContainer, childSceneContainerParams)
        childSceneContainer.id = View.generateViewId()
        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.counter.observe(this, Observer<Int> { t -> textView.text = "" + t })

        add(childSceneContainer.id, ViewModelSceneSamplesChild(), "Child")
    }
}

class ViewModelSceneSamplesChild : Scene() {
    private val viewModel: SampleViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return Button(requireSceneContext()).apply {
            text = "Click to +1"
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireView().setOnClickListener {
            val countValue = viewModel.counter.value ?: 0
            viewModel.counter.value = countValue + 1
        }
    }
}

class SampleViewModel : ViewModel() {
    val counter: MutableLiveData<Int> = MutableLiveData()
}