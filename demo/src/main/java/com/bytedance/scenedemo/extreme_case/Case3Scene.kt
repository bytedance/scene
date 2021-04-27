package com.bytedance.scenedemo.extreme_case

import android.animation.Animator
import com.bytedance.scene.group.GroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.bytedance.scenedemo.R
import android.widget.TextView
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.animation.NavigationAnimatorExecutor
import android.animation.ValueAnimator
import android.animation.ObjectAnimator
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.bytedance.scene.Scene
import com.bytedance.scene.animation.AnimationInfo
import com.bytedance.scene.animation.TransitionUtils
import com.bytedance.scene.ktx.navigationScene
import com.bytedance.scenedemo.lifecycle.EmptyScene
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 9/5/18.
 */
class Case3Scene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 0))
        val name = view.findViewById<TextView>(R.id.name)
        name.visibility = View.GONE
        val btn = view.findViewById<Button>(R.id.btn)
        btn.setText(R.string.case_remove_btn)
        btn.setOnClickListener {
            navigationScene!!.push(EmptyScene::class.java, null, PushOptions.Builder().setAnimation(AAA()).build())
            navigationScene!!.remove(this@Case3Scene)
        }
    }

    private class AAA : NavigationAnimatorExecutor() {
        override fun isSupport(from: Class<out Scene?>, to: Class<out Scene?>): Boolean {
            return true
        }

        override fun disableConfigAnimationDuration(): Boolean {
            return true
        }

        override fun onPushAnimator(from: AnimationInfo, to: AnimationInfo): Animator {
            val fromView = from.mSceneView
            val toView = to.mSceneView
            val fromAlphaAnimator: ValueAnimator =
                ObjectAnimator.ofFloat(fromView, View.ALPHA, 1.0f, 1.0f) //之前是0.7，但是动画后面会露出NavigationScene的背景色白色很怪异
            fromAlphaAnimator.interpolator = FastOutSlowInInterpolator()
            fromAlphaAnimator.duration = (120 * 20).toLong()
            val toAlphaAnimator: ValueAnimator = ObjectAnimator.ofFloat(toView, View.ALPHA, 0.0f, 1.0f)
            toAlphaAnimator.interpolator = DecelerateInterpolator(2f)
            toAlphaAnimator.duration = (120 * 20).toLong()
            val toTranslateAnimator: ValueAnimator =
                ObjectAnimator.ofFloat(toView, View.TRANSLATION_Y, 0.08f * toView.height, 0f)
            toTranslateAnimator.interpolator = DecelerateInterpolator(2.5f)
            toTranslateAnimator.duration = (200 * 20).toLong()
            return TransitionUtils.mergeAnimators(fromAlphaAnimator, toAlphaAnimator, toTranslateAnimator)
        }

        override fun onPopAnimator(fromInfo: AnimationInfo, toInfo: AnimationInfo): Animator {
            val toView = toInfo.mSceneView
            val fromView = fromInfo.mSceneView
            val fromAlphaAnimator: ValueAnimator = ObjectAnimator.ofFloat(fromView, View.ALPHA, 1.0f, 0.0f)
            fromAlphaAnimator.interpolator = LinearInterpolator()
            fromAlphaAnimator.duration = (150 * 20).toLong()
            fromAlphaAnimator.startDelay = (50 * 20).toLong()
            val fromTranslateAnimator: ValueAnimator =
                ObjectAnimator.ofFloat(fromView, View.TRANSLATION_Y, 0f, 0.08f * toView.height)
            fromTranslateAnimator.interpolator = AccelerateInterpolator(2f)
            fromTranslateAnimator.duration = (200 * 20).toLong()
            val toAlphaAnimator: ValueAnimator = ObjectAnimator.ofFloat(toView, View.ALPHA, 0.7f, 1.0f)
            toAlphaAnimator.interpolator = LinearOutSlowInInterpolator()
            toAlphaAnimator.duration = (20 * 20).toLong()
            return TransitionUtils.mergeAnimators(fromAlphaAnimator, fromTranslateAnimator, toAlphaAnimator)
        }
    }
}