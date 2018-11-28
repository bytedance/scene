package com.bytedance.scene.interfaces;

import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by JiangQi on 8/3/18.
 */
public interface ActivityResultCallback {
    void onResult(int resultCode, @Nullable Intent result);
}
