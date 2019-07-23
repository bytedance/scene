package com.bytedance.scene.animation.interaction.scenetransition.utils;

import android.graphics.Matrix;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.ImageView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RequiresApi(21)
class ImageViewRefUtils {
    private static final String TAG = "ImageViewRefUtils";

    private static Method sAnimateTransformMethod;
    private static boolean sAnimateTransformMethodFetched;

    public void animateTransform(ImageView view, Matrix matrix) {
        fetchAnimateTransformMethod();
        if (sAnimateTransformMethod != null) {
            try {
                sAnimateTransformMethod.invoke(view, matrix);
            } catch (IllegalAccessException e) {
                // Do nothing
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    private void fetchAnimateTransformMethod() {
        if (!sAnimateTransformMethodFetched) {
            try {
                sAnimateTransformMethod = ImageView.class.getDeclaredMethod("animateTransform",
                        Matrix.class);
                sAnimateTransformMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                Log.i(TAG, "Failed to retrieve animateTransform method", e);
            }
            sAnimateTransformMethodFetched = true;
        }
    }
}
