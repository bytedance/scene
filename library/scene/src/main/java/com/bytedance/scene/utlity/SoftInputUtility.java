package com.bytedance.scene.utlity;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by JiangQi on 8/19/18.
 */
public class SoftInputUtility {
    public static boolean hideSoftInputFromWindow(View view) {
        return view != null && ((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean hideSoftInputFromWindow(Window window) {
        return hideSoftInputFromWindow(window != null ? window.getDecorView() : null);
    }
}
