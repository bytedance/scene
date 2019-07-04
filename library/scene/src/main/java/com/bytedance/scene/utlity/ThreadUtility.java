package com.bytedance.scene.utlity;

import android.os.Looper;
import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 9/5/18.
 */

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ThreadUtility {
    public static void checkUIThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("This method must call on main thread");
        }
    }
}
