package com.bytedance.scene;

/**
 * NONE 什么都没，或者没开始，或者销毁
 * STOPPED 有了View，但是没显示
 * STARTED 局部显示
 * RESUMED 全部显示
 */

import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public enum State {
    NONE(0, "NONE"), STOPPED(1, "STOPPED"), STARTED(2, "STARTED"), RESUMED(3, "RESUMED");

    public final int value;
    public final String name;

    State(int value, String name) {
        this.value = value;
        this.name = name;
    }
}
