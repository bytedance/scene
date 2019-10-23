/*
 * Copyright (C) 2019 ByteDance Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bytedance.scene.ktx

import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.IdRes
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
    replace(viewId, scene, tag, 0)
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

