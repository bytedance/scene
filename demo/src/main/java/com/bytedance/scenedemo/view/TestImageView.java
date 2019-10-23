package com.bytedance.scenedemo.view;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by JiangQi on 9/3/18.
 */
public class TestImageView extends AppCompatImageView {
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
