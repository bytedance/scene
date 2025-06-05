package com.bytedance.scene.utlity

import android.provider.Settings
import android.view.View
import androidx.annotation.RestrictTo
import androidx.annotation.StringDef
import kotlin.math.max

/**
 * Created by jiangqi on 2024/9/27
 * @author jiangqi@bytedance.com
 */
/**
 * if system > Build.VERSION_CODES.TIRAMISU, can use ValueAnimator.getDurationScale() instead
 */
internal fun getDurationScale(view: View): Float {
    return getDurationScale(view, Settings.Global.ANIMATOR_DURATION_SCALE)
}

@StringDef(
    value = [Settings.Global.ANIMATOR_DURATION_SCALE, Settings.Global.TRANSITION_ANIMATION_SCALE, Settings.Global.WINDOW_ANIMATION_SCALE]
)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class DurationScaleType

internal fun getDurationScale(view: View, @DurationScaleType durationScaleType: String): Float {
    val durationScale = Settings.Global.getFloat(
        view.context.contentResolver, durationScaleType, 1.0f
    )
    return max(0.0f, durationScale)
}

/** @hide */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun areAnimationEnabled(
    view: View, @DurationScaleType durationScaleType: String
): Boolean {
    val durationScale = Settings.Global.getFloat(
        view.context.contentResolver, durationScaleType, 1.0f
    )
    return durationScale != 0.0f
}