package com.bytedance.scene.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.bytedance.scene.ui.R;

/**
 * Created by JiangQi on 8/28/18.
 */
public class StatusBarView extends View {
    private static final int[] THEME_ATTRS = {
            R.attr.colorPrimaryDark
    };

    private WindowInsetsCompat mLastInsets;
    private boolean mDrawStatusBarBackground = true;
    private Drawable mStatusBarBackground;

    public StatusBarView(Context context) {
        super(context);
        init();
    }

    public StatusBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatusBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ViewCompat.setOnApplyWindowInsetsListener(this,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        mLastInsets = new WindowInsetsCompat(insets);
                        requestLayout();
                        return new WindowInsetsCompat(insets);
                    }
                });

        final TypedArray a = getContext().obtainStyledAttributes(THEME_ATTRS);
        try {
            mStatusBarBackground = a.getDrawable(0);
        } finally {
            a.recycle();
        }

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics());
        ViewCompat.setElevation(this, px);
    }

    public void setStatusBarBackground(Drawable bg) {
        mStatusBarBackground = bg;
        invalidate();
    }

    public Drawable getStatusBarBackgroundDrawable() {
        return mStatusBarBackground;
    }

    public void setStatusBarBackground(int resId) {
        mStatusBarBackground = resId != 0 ? ContextCompat.getDrawable(getContext(), resId) : null;
        invalidate();
    }

    public void setStatusBarBackgroundColor(@ColorInt int color) {
        mStatusBarBackground = new ColorDrawable(color);
        invalidate();
    }

    private static int getDefaultSize2(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(size, specSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    protected void onMeasure2(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                getDefaultSize2(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize2(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mLastInsets != null) {
            onMeasure2(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mLastInsets.getSystemWindowInsetTop(), MeasureSpec.EXACTLY));
        } else {
            onMeasure2(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawStatusBarBackground && mStatusBarBackground != null) {
            final int inset;
            if (Build.VERSION.SDK_INT >= 21) {
                inset = mLastInsets != null
                        ? mLastInsets.getSystemWindowInsetTop() : 0;
            } else {
                inset = 0;
            }
            if (inset > 0) {
                mStatusBarBackground.setBounds(0, 0, getWidth(), inset);
                mStatusBarBackground.draw(canvas);
            }
        }
    }
}
