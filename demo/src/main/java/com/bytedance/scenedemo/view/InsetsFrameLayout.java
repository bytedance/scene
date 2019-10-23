package com.bytedance.scenedemo.view;

import android.content.Context;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class InsetsFrameLayout extends FrameLayout {

    public InsetsFrameLayout(Context context) {
        this(context, null);
    }

    public InsetsFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InsetsFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ViewCompat.setOnApplyWindowInsetsListener(this,
                new androidx.core.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        setPadding(insets.getSystemWindowInsetLeft(),
                                insets.getSystemWindowInsetTop(),
                                insets.getSystemWindowInsetRight(),
                                0);
                        ViewCompat.postInvalidateOnAnimation(InsetsFrameLayout.this);
                        return insets;
                    }
                });
    }
}
