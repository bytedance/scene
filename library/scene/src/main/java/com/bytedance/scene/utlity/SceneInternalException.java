package com.bytedance.scene.utlity;

import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class SceneInternalException extends RuntimeException {
    public SceneInternalException(String message) {
        super(message);
    }

    public SceneInternalException(String message, Throwable cause) {
        super(message, cause);
    }

    public SceneInternalException(Throwable cause) {
        super(cause);
    }
}
