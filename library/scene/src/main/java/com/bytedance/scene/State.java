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
package com.bytedance.scene;

import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @hide
 * NONE             : Nothing, not started or has been destroyed
 * VIEW_CREATED     : View created, but not showing now
 * ACTIVITY_CREATED : onActivityCreated has completed
 * STARTED          : Partial showing
 * RESUMED          : Completely showing
 */
@RestrictTo(LIBRARY_GROUP)
public enum State {
    NONE(0, "NONE"),
    CREATED(1, "CREATED"),
    VIEW_CREATED(2, "VIEW_CREATED"),
    ACTIVITY_CREATED(3, "ACTIVITY_CREATED"),
    STARTED(4, "STARTED"),
    RESUMED(5, "RESUMED");

    public final int value;
    public final String name;

    State(int value, String name) {
        this.value = value;
        this.name = name;
    }

    //avoid kotlin overload resolution ambiguity
    public String getName() {
        return this.name;
    }
}
