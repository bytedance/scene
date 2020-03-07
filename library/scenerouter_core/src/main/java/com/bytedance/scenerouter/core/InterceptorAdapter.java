package com.bytedance.scenerouter.core;

import android.support.annotation.NonNull;

interface InterceptorAdapter {
    void run(@NonNull TaskInfo taskInfo, @NonNull ContinueTask task);
}
