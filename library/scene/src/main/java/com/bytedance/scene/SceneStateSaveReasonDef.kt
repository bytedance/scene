package com.bytedance.scene

import androidx.annotation.IntDef

@IntDef(
    SceneStateSaveReason.UNKNOWN,
    SceneStateSaveReason.PARENT_SAVED,
    SceneStateSaveReason.RECYCLING,
    SceneStateSaveReason.CONFIGURATION_CHANGED
)
@Retention(AnnotationRetention.SOURCE)
annotation class SceneStateSaveReasonDef