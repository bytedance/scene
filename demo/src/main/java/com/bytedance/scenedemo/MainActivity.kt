package com.bytedance.scenedemo

import com.bytedance.scene.Scene
import com.bytedance.scene.ui.SceneActivity

class MainActivity : SceneActivity() {
    override fun getHomeSceneClass(): Class<out Scene> {
        return MainScene::class.java
    }

    override fun supportRestore(): Boolean {
        return true
    }
}
