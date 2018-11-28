package com.bytedance.scene.interfaces;

import android.support.annotation.Nullable;

/**
 * Created by JiangQi on 8/10/18.
 */
public interface PermissionResultCallback {
    void onResult(@Nullable int[] grantResults);
}
