package com.bytedance.scenedemo.animation.fullsharedelement

import com.bytedance.scene.group.GroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.collection.ArrayMap
import com.bytedance.scenedemo.R
import com.bytedance.scene.animation.SharedElementSceneTransitionExecutor
import com.bytedance.scene.animation.interaction.scenetransition.*
import com.bytedance.scene.animation.interaction.scenetransition.visiblity.Slide
import com.bytedance.scenedemo.animation.fullsharedelement.FullSharedElementAnimationScene1
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.ktx.requireNavigationScene

/**
 * Created by JiangQi on 10/19/18.
 */
class FullSharedElementAnimationScene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.full_shared_element_0, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val map = ArrayMap<String, SceneTransition>()
        val a = SceneTransitionSet()
        a.addSceneTransition(ChangeTransform())
        a.addSceneTransition(ChangeBounds())
        map["haha"] = a
        map["imageView"] = AutoSceneTransition().addSceneTransition(ChangeImageTransform())
        map["haha_parent"] = AutoSceneTransition().addSceneTransition(BackgroundRecolor())
        map["shared_element_text_no_size_change"] =
            AutoSceneTransition().addSceneTransition(BackgroundRecolor()).addSceneTransition(TextRecolor())
        //        map.put("shared_element_text_no_size_change", new AutoSceneTransition());
        val sharedElementSceneTransitionExecutor = SharedElementSceneTransitionExecutor(map, Slide())
        findViewById<View>(R.id.haha)?.setOnClickListener {
            requireNavigationScene().push(
                FullSharedElementAnimationScene1(),
                PushOptions.Builder().setAnimation(sharedElementSceneTransitionExecutor).build()
            )
        }
    }
}