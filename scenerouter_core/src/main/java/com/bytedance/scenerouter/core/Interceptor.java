package com.bytedance.scenerouter.core;

import android.support.annotation.NonNull;

public interface Interceptor {
    void process(@NonNull TaskInfo taskInfo, @NonNull ContinueTask continueTask);
}
