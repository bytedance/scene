package com.bytedance.scene;

import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

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
    VIEW_CREATED(1, "VIEW_CREATED"),
    ACTIVITY_CREATED(2, "ACTIVITY_CREATED"),
    STARTED(3, "STARTED"),
    RESUMED(4, "RESUMED");

    public final int value;
    public final String name;

    State(int value, String name) {
        this.value = value;
        this.name = name;
    }
}
