package com.bytedance.scene.fragment

import androidx.fragment.app.Fragment
import com.bytedance.scene.ktx.getNavigationScene
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scene.navigation.NavigationScene


/**
 * return the target NavigationScene which bind to current Fragment
 */
fun Fragment.requireNavigationScene(): NavigationScene {
    val view = this.view ?: throw IllegalStateException("Fragment is not created")
    return view.requireNavigationScene()
}

fun Fragment.getNavigationScene(): NavigationScene? {
    val view = this.view
    return view?.getNavigationScene()
}

