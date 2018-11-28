package com.bytedance.scenedemo.async_inflate;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by JiangQi on 9/19/18.
 */
public class TestAsyncView extends View {
    public TestAsyncView(Context context) {
        super(context);
    }

    public TestAsyncView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TestAsyncView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TestAsyncView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        Log.e("ddd", "sss");
    }
}
