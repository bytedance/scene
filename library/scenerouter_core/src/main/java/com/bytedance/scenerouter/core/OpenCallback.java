package com.bytedance.scenerouter.core;

import android.support.annotation.Nullable;

public interface OpenCallback {
    void onSuccess();

    void onFail(@Nullable Exception exception);
}
