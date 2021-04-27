package com.bytedance.scenedemo.animation

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.bytedance.scene.view.SlidePercentFrameLayout
import com.bytedance.scenedemo.R
import android.widget.LinearLayout
import com.bytedance.scene.animation.interaction.InteractionNavigationPopAnimationFactory
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation
import com.bytedance.scenedemo.MainScene
import com.bytedance.scenedemo.AnimationListDemoScene
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimationBuilder
import com.bytedance.scene.animation.interaction.progressanimation.DrawableAnimationBuilder
import com.bytedance.scene.interfaces.PopOptions
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor
import android.widget.TextView
import android.view.Gravity
import android.view.View
import android.widget.Button
import com.bytedance.scene.Scene
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scenedemo.utility.ColorUtil
import java.util.ArrayList

/**
 * Created by JiangQi on 8/22/18.
 */
class SlideBackButtonDemoScene : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val layout = SlidePercentFrameLayout(activity!!)
        layout.fitsSystemWindows = true
        val button = Button(activity)
        button.isAllCaps = false
        button.setText(R.string.main_anim_btn_ios_anim)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150)
        lp.topMargin = 20
        lp.leftMargin = 20
        lp.rightMargin = 20
        layout.addView(button, lp)
        val interactionNavigationPopAnimationFactory: InteractionNavigationPopAnimationFactory =
            object : InteractionNavigationPopAnimationFactory() {
                override fun isSupport(from: Scene, to: Scene): Boolean {
                    return true
                }

                override fun onPopInteraction(from: Scene, to: Scene): List<InteractionAnimation> {
                    val mainScene = to as MainScene
                    val animationListDemoScene = findTargetScene(mainScene)
                    val buttonLocation = IntArray(2)
                    button.getLocationInWindow(buttonLocation)
                    val buttonLocation2 = IntArray(2)
                    animationListDemoScene!!.mInteractionButton!!.getLocationInWindow(buttonLocation2)
                    val a: MutableList<InteractionAnimation> = ArrayList()
                    a.add(
                        InteractionAnimationBuilder.with(button)
                            .translationXBy((buttonLocation2[0] - buttonLocation[0]).toFloat()).endProgress(0.5f)
                            .build()
                    )
                    a.add(
                        InteractionAnimationBuilder.with(button)
                            .translationYBy((buttonLocation2[1] - buttonLocation[1]).toFloat()).endProgress(0.5f)
                            .build()
                    )
                    a.add(DrawableAnimationBuilder.with(view.background).alpha(255, 0).endProgress(0.5f).build())
                    return a
                }

                override fun canExit(progress: Float): Boolean {
                    return progress > 0.3f
                }

                override fun onInteractionCancel() {}
                override fun onInteractionEnd() {
                    requireNavigationScene().pop(PopOptions.Builder().setAnimation(NoAnimationExecutor()).build())
                }
            }
        layout.setCallback(object : SlidePercentFrameLayout.Callback {
            override fun isSupport(): Boolean {
                return true
            }

            override fun onStart() {
                requireNavigationScene().pop(interactionNavigationPopAnimationFactory)
                requireNavigationScene().convertBackgroundToBlack()
            }

            override fun onFinish() {
                interactionNavigationPopAnimationFactory.finish()
            }

            override fun onProgress(progress: Float) {
                interactionNavigationPopAnimationFactory.updateProgress(progress)
            }
        })
        layout.setBackgroundColor(ColorUtil.getMaterialColor(resources, 1))
        val textView = TextView(activity)
        textView.setPadding(0, 400, 0, 0)
        textView.setText(R.string.anim_ios_interaction_tip)
        textView.gravity = Gravity.CENTER
        layout.addView(
            textView,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )
        return layout
    }

    companion object {
        private fun findTargetScene(groupScene: GroupScene): AnimationListDemoScene? {
            val childSceneList = groupScene.sceneList
            for (i in childSceneList.indices) {
                val scene = childSceneList[i]
                if (scene is AnimationListDemoScene) {
                    return scene
                } else if (scene is GroupScene) {
                    val animationListDemoScene = findTargetScene(scene)
                    if (animationListDemoScene != null) {
                        return animationListDemoScene
                    }
                }
            }
            return null
        }
    }
}