package com.bytedance.scene.ktx

import android.support.annotation.IdRes
import com.bytedance.scene.Scene
import com.bytedance.scene.group.GroupScene

/**
 * Created by JiangQi on 2019-07-29.
 */
fun GroupScene.show(tag: String) {
    val scene = findSceneByTag<Scene>(tag)
    if (scene != null) {
        show(scene)
    }
}

fun GroupScene.hide(tag: String) {
    val scene = findSceneByTag<Scene>(tag)
    if (scene != null) {
        hide(scene)
    }
}

fun GroupScene.remove(tag: String) {
    val scene = findSceneByTag<Scene>(tag)
    if (scene != null) {
        remove(scene)
    }
}

fun GroupScene.addAndHide(@IdRes viewId: Int, scene: Scene, tag: String) {
    beginTransaction()
    add(viewId, scene, tag)
    hide(scene)
    commitTransaction()
}