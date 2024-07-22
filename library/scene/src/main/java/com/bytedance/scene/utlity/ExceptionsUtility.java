package com.bytedance.scene.utlity;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * Created by jiangqi on 2023/10/20
 *
 * @author jiangqi@bytedance.com
 */
@RestrictTo(LIBRARY_GROUP)
public class ExceptionsUtility {
    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    public static void invokeAndThrowExceptionToNextUILoop(@NonNull Runnable runnable) {
        try {
            runnable.run();
        } catch (final Throwable throwable) {
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    throw throwable;
                }
            });
            throw throwable;
        }
    }
}
