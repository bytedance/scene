package com.bytedance.scenerouter.core;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class TaskInfo {
    @NonNull
    public final String url;

    @Nullable
    public final Bundle arguments;

    TaskInfo(@NonNull String url, @Nullable Bundle arguments) {
        this.url = url;
        this.arguments = arguments;
    }
}
