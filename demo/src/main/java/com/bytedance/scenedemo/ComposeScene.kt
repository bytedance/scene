package com.bytedance.scenedemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.bytedance.scene.Scene
import com.bytedance.scene.ktx.fragmentActivity

open class ComposeScene : Scene() {
    private lateinit var composeView: ComposeView
    override fun onCreateView(p0: LayoutInflater, p1: ViewGroup, p2: Bundle?): View {
        composeView = ComposeView(requireSceneContext())
        return composeView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewTreeLifecycleOwner.set(this.view, this)
        ViewTreeViewModelStoreOwner.set(this.view, this)
        ViewTreeSavedStateRegistryOwner.set(this.view, fragmentActivity())

        composeView.setParentCompositionContext(createLifecycleAwareViewTreeRecomposer())
    }

    fun requireComposeView() = composeView
}