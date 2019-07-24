package com.bytedance.scene.parcel;

/**
 * Created by JiangQi on 8/8/18.
 */

import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ParcelConstants {
    public static final String KEY_SCENE_HAS_ARGUMENT = "bd-scene-nav:scene_argument_has";
    public static final String KEY_SCENE_ARGUMENT = "bd-scene-nav:scene_argument";

    public static final String KEY_NAVIGATION_RECORD_LIST = "bd-scene-nav:record_stack";
    public static final String KEY_NAVIGATION_SCENE_MANAGER_TAG = "bd-scene-nav:navigation_scene_manager";

    public static final String KEY_GROUP_RECORD_LIST = "bd-scene-nav:group_stack";
    public static final String KEY_GROUP_SCENE_MANAGER_TAG = "bd-scene-nav:group_scene_manager";

    public static final String KEY_SCENE_VIEWS_TAG = "bd-scene:views";
    public static final String KEY_SCENE_FOCUSED_ID_TAG = "bd-scene:focusedViewId";
}
