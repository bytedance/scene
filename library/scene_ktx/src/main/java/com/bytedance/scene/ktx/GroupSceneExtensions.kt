package com.bytedance.scene.ktx

import android.support.annotation.AnimRes
import android.support.annotation.AnimatorRes
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

fun GroupScene.replace(@IdRes viewId: Int, scene: Scene, tag: String) {
    val previousScene = findSceneByTag<Scene>(tag)
    if (previousScene == scene) {
        return
    }

    if (isAdded(scene)) {
        remove(scene)
    }

    if (previousScene == null) {
        add(viewId, scene, tag)
        return
    }

    if (isShow(previousScene)) {
        remove(previousScene)
        add(viewId, scene, tag)
    } else {
        remove(previousScene)
        addAndHide(viewId, scene, tag)
    }
}

fun GroupScene.replace(@IdRes viewId: Int, scene: Scene, tag: String, @AnimRes @AnimatorRes animationResId: Int) {
    val previousScene = findSceneByTag<Scene>(tag)
    if (previousScene == scene) {
        return
    }

    if (isAdded(scene)) {
        remove(scene)
    }

    if (previousScene == null) {
        add(viewId, scene, tag, animationResId)
        return
    }

    if (isShow(previousScene)) {
        remove(previousScene)
        add(viewId, scene, tag, animationResId)
    } else {
        remove(previousScene)
        addAndHide(viewId, scene, tag)
    }
}

