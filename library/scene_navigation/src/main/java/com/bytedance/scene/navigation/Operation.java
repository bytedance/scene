package com.bytedance.scene.navigation;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.RestrictTo;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public interface Operation {
    void execute(Runnable operationEndAction);
}