package com.bytedance.scene.animation.interaction.scenetransition.utils;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ViewGroup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by JiangQi on 10/19/18.
 */
class SuppressLayoutUtils {
    private static final String TAG = "SuppressLayoutUtils";

    private static Method sSuppressLayoutMethod;
    private static boolean sSuppressLayoutMethodFetched;


    public void suppressLayout(@NonNull ViewGroup group, boolean suppress) {
        fetchSuppressLayoutMethod();
        if (sSuppressLayoutMethod != null) {
            try {
                sSuppressLayoutMethod.invoke(group, suppress);
            } catch (IllegalAccessException e) {
                Log.i(TAG, "Failed to invoke suppressLayout method", e);
            } catch (InvocationTargetException e) {
                Log.i(TAG, "Error invoking suppressLayout method", e);
            }
        }
    }

    private static void fetchSuppressLayoutMethod() {
        if (!sSuppressLayoutMethodFetched) {
            try {
                sSuppressLayoutMethod = ViewGroup.class.getDeclaredMethod("suppressLayout", boolean.class);
                sSuppressLayoutMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                Log.i(TAG, "Failed to retrieve suppressLayout method", e);
            }
            sSuppressLayoutMethodFetched = true;
        }
    }
}
