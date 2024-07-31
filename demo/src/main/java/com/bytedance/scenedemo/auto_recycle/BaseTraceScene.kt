package com.bytedance.scenedemo.auto_recycle

import android.os.Bundle
import com.bytedance.scene.Scene
import com.bytedance.scene.ktx.activityViewModels

abstract class BaseTraceScene : Scene() {

    protected val viewModel: AutoRecycleSceneViewModel by activityViewModels()

    abstract val name: String

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.sceneOnCreate("$name@${this.hashCode()}", savedInstanceState)
        viewModel.updateMemoryUsed()
    }

    override fun onStart() {
        super.onStart()
        viewModel.sceneOnStart("$name@${this.hashCode()}")
    }

    override fun onResume() {
        super.onResume()
        viewModel.sceneOnResume("$name@${this.hashCode()}")
    }

    override fun onPause() {
        super.onPause()
        viewModel.sceneOnPause("$name@${this.hashCode()}")
    }

    override fun onStop() {
        super.onStop()
        viewModel.sceneOnStop("$name@${this.hashCode()}")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.sceneOnSaveStated("$name@${this.hashCode()}", outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.sceneOnDestroy("$name@${this.hashCode()}")
    }

}