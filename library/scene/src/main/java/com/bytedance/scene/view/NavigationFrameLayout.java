package com.bytedance.scene.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.AttributeSet;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 7/30/18.
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class NavigationFrameLayout extends NoneTouchFrameLayout {
    public NavigationFrameLayout(@NonNull Context context) {
        super(context);
    }

    public NavigationFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NavigationFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NavigationFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
