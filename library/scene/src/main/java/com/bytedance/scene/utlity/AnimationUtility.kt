package com.bytedance.scene.utlity

import android.provider.Settings
import android.view.View
import kotlin.math.max

/**
 * Created by jiangqi on 2024/9/27
 * @author jiangqi@bytedance.com
 */
/**
 * if system > Build.VERSION_CODES.TIRAMISU, can use ValueAnimator.getDurationScale() instead
 *
 * Scene follow [Settings.Global.ANIMATOR_DURATION_SCALE] but Activity follow [Settings.Global.TRANSITION_ANIMATION_SCALE]
 */
internal fun getDurationScale(view: View): Float {
    val durationScale = Settings.Global.getFloat(
        view.context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f
    )
    return max(0.0f, durationScale)
}