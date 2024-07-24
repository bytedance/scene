/*
 * Copyright (C) 2019 ByteDance Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bytedance.scene.parcel;

/**
 * Created by JiangQi on 8/8/18.
 */

import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

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
    public static final String KEY_SCENE_SCOPE_KEY_TAG = "bd-scene:scope_key";
}
