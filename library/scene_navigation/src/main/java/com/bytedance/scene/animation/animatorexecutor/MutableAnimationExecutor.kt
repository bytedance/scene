package com.bytedance.scene.animation.animatorexecutor

import android.view.ViewGroup
import com.bytedance.scene.Scene
import com.bytedance.scene.animation.AnimationInfo
import com.bytedance.scene.animation.NavigationAnimationExecutor
import com.bytedance.scene.utlity.CancellationSignal

/**
 * Mutable animation executor which can modifies executor before start an animation.
 *
 * Created by junyu on 2025/5/8
 * @author yuejunyu.0@tiktok.com
 */
class MutableAnimationExecutor(
    // note: setter functions e.g. setAnimationViewGroup
    // will not be called again when the delegated being replaced.
    var delegated: NavigationAnimationExecutor,
) : NavigationAnimationExecutor() {
    override fun setDisableRemoveView(disableRemoveView: Boolean) {
        super.setDisableRemoveView(disableRemoveView)
        delegated.setDisableRemoveView(disableRemoveView)
    }

    override fun setAnimationEndAction(endAction: Runnable?) {
        super.setAnimationEndAction(endAction)
        delegated.setAnimationEndAction(endAction)
    }

    override fun setAnimationViewGroup(viewGroup: ViewGroup) {
        super.setAnimationViewGroup(viewGroup)
        delegated.setAnimationViewGroup(viewGroup)
    }

    override fun executePopChangeCancelable(
        fromInfo: AnimationInfo, toInfo: AnimationInfo, endAction: Runnable, cancellationSignal: CancellationSignal
    ) = delegated.executePopChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal)

    override fun executePushChangeCancelable(
        fromInfo: AnimationInfo, toInfo: AnimationInfo, endAction: Runnable, cancellationSignal: CancellationSignal
    ) = delegated.executePushChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal)

    override fun isSupport(from: Class<out Scene?>, to: Class<out Scene?>): Boolean {
        return delegated.isSupport(from, to)
    }
}