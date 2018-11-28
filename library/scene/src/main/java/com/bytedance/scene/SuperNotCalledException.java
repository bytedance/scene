package com.bytedance.scene;

import android.util.AndroidRuntimeException;

final class SuperNotCalledException extends AndroidRuntimeException {
    public SuperNotCalledException(String msg) {
        super(msg);
    }
}