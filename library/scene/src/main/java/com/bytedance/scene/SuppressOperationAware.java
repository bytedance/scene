package com.bytedance.scene;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

@RestrictTo(LIBRARY_GROUP)
public interface SuppressOperationAware {
    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @NonNull
    String beginSuppressStackOperation(@NonNull String tagPrefix);

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    void endSuppressStackOperation(@NonNull String suppressTag);
}
