package com.bytedance.scene.utlity;

import android.os.Looper;

/**
 * Created by JiangQi on 9/5/18.
 */
public class ThreadUtility {
    public static void checkUIThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("This method must call on main thread");
        }
    }
}
