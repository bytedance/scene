package com.bytedance.scenedemo.navigation.push_pop;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.LinearLayout;

/**
 * Created by JiangQi on 8/24/18.
 */
public class Test extends LinearLayout {
    public Test(Context context) {
        super(context);
    }

    public Test(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Test(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Test(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        return super.onApplyWindowInsets(insets);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        return super.fitSystemWindows(insets);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        super.setPaddingRelative(start, top, end, bottom);
    }
}
