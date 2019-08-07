package com.bytedance.scenedemo.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by JiangQi on 9/3/18.
 */
public class TestImageView extends android.support.v7.widget.AppCompatImageView {
    public TestImageView(Context context) {
        super(context);
    }

    public TestImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TestImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setRotation(float rotation) {
        super.setRotation(rotation);
    }
}
