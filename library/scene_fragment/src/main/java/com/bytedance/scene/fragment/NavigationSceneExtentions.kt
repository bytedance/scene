package com.bytedance.scene.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.navigation.NavigationScene

fun NavigationScene.push(fragment: Fragment, pushOptions: PushOptions? = null) {
    val scene = FragmentScene.newInstance(fragment.javaClass, fragment.arguments)
    scene.rootFragment = fragment
    push(scene, pushOptions)
}

fun NavigationScene.push(
    fragmentClass: Class<out Fragment>, arguments: Bundle? = null, pushOptions: PushOptions? = null
) {
    val scene = FragmentScene.newInstance(fragmentClass, arguments)
    push(scene, pushOptions)
}