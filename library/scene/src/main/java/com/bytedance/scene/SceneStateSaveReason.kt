package com.bytedance.scene

//Describes the reason why a Scene's state is being saved.
object SceneStateSaveReason {
    const val KEY_SCENE_SAVE_STATE_REASON = "bd-scene:scene_save_instance_state_reason";

    const val UNKNOWN = 0

    /**
     * The state is saved because the parent container (e.g., an Activity or Fragment)
     * is going through its own state-saving process.
     */
    const val PARENT_SAVED = 1

    /**
     * The state is saved as part of a process to recycle invisible Scenes
     * to optimize memory and performance.
     */
    const val RECYCLING = 2

    /**
     * The state is saved because the Scene's own configuration has changed,
     * such as a theme or orientation change.
     */
    const val CONFIGURATION_CHANGED = 3
}

