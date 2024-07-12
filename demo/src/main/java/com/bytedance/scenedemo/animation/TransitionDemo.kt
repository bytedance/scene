package com.bytedance.scenedemo.animation

import android.os.Build
import androidx.annotation.RequiresApi
import com.bytedance.scene.group.GroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.bytedance.scenedemo.R
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scene.animation.NavigationTransitionExecutor
import android.view.Gravity
import android.view.View
import androidx.transition.AutoTransition
import androidx.transition.ChangeTransform
import androidx.transition.Slide
import androidx.transition.Transition
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/23/18.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
class TransitionDemo : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.layout_transition_0, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.findViewById<View>(R.id.btn).setOnClickListener {
            requireNavigationScene().push(
                TransitionDemo2::class.java, null, PushOptions.Builder().setAnimation(
                    Test()
                ).build()
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView().setBackgroundColor(ColorUtil.getMaterialColor(resources, 1))
    }

    class TransitionDemo2 : GroupScene() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup,
            savedInstanceState: Bundle?
        ): ViewGroup {
            return inflater.inflate(R.layout.layout_transition_1, container, false) as ViewGroup
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            getView().setBackgroundColor(ColorUtil.getMaterialColor(resources, 2))
        }
    }

    internal inner class Test : NavigationTransitionExecutor() {
        override fun getSharedElementTransition(): Transition {
            val autoTransition: Transition = AutoTransition().addTransition(ChangeTransform())
            autoTransition.addTarget("wo")
            autoTransition.addTarget("imageview")
            return autoTransition
        }

        override fun getOthersTransition(): Transition {
            val slide = Slide()
            slide.slideEdge = Gravity.BOTTOM
            return slide
        }
    }
}