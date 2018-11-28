package com.bytedance.scene.animation.interaction.scenetransition.utils;

import android.annotation.SuppressLint;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RequiresApi(22)
class LeftTopRightBottomRefUtils {
    private static final String TAG = "LeftTopRightBottom";

    private static Method sSetLeftTopRightBottomMethod;
    private static boolean sSetLeftTopRightBottomMethodFetched;

    public void setLeftTopRightBottom(View v, int left, int top, int right, int bottom) {
        fetchSetLeftTopRightBottomMethod();
        if (sSetLeftTopRightBottomMethod != null) {
            try {
                sSetLeftTopRightBottomMethod.invoke(v, left, top, right, bottom);
            } catch (IllegalAccessException e) {
                // Do nothing
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    @SuppressLint("PrivateApi")
    private void fetchSetLeftTopRightBottomMethod() {
        if (!sSetLeftTopRightBottomMethodFetched) {
            try {
                sSetLeftTopRightBottomMethod = View.class.getDeclaredMethod("setLeftTopRightBottom",
                        int.class, int.class, int.class, int.class);
                sSetLeftTopRightBottomMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                Log.i(TAG, "Failed to retrieve setLeftTopRightBottom method", e);
            }
            sSetLeftTopRightBottomMethodFetched = true;
        }
    }
}