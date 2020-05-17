package org.qiibeta.lib2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.ui.template.AppCompatScene
import com.bytedance.scenerouter.annotation.SceneUrl


@SceneUrl("/lib2/detail")
class Library2DetailScene : AppCompatScene() {
    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        return View(requireSceneContext())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTitle("/lib2/detail")
    }
}
