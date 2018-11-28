package com.bytedance.scenedemo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by JiangQi on 9/3/18.
 */
public class TestImageView extends ImageView {
    public TestImageView(Context context) {
        super(context);
    }

    public TestImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TestImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TestImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setRotation(float rotation) {
        super.setRotation(rotation);
    }
}
