package com.bytedance.scene.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by JiangQi on 7/31/18.
 */
public class NoneTouchFrameLayout extends FrameLayout {
    private boolean mIsTouchEnabled = true;

    public NoneTouchFrameLayout(@NonNull Context context) {
        super(context);
    }

    public NoneTouchFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NoneTouchFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NoneTouchFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!mIsTouchEnabled) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setTouchEnabled(boolean enabled) {
        this.mIsTouchEnabled = enabled;
    }
}
