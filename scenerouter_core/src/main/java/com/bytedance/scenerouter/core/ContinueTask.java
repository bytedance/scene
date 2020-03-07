package com.bytedance.scenerouter.core;

import android.support.annotation.Nullable;

public interface ContinueTask {
    void onContinue();

    void onFail(@Nullable Exception exception);
}
