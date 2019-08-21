package com.bytedance.scene.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.bytedance.scene.ui.R;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 8/28/18.
 */
public final class NavigationBarView extends View {
    private WindowInsetsCompat mLastInsets;
    private boolean mDrawNavigationBarBackground = true;
    private Drawable mNavigationBarBackground;

    public NavigationBarView(Context context) {
        super(context);
        init();
    }

    public NavigationBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NavigationBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ViewCompat.setOnApplyWindowInsetsListener(this,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        if (getVisibility() == View.GONE) {
                            mLastInsets = null;
                            return insets;
                        }
                        mLastInsets = new WindowInsetsCompat(insets);
                        requestLayout();
                        return new WindowInsetsCompat(insets).replaceSystemWindowInsets(
                                insets.getSystemWindowInsetLeft(),
                                insets.getSystemWindowInsetTop(),
                                insets.getSystemWindowInsetRight(),
                                0);
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final int[] THEME_ATTRS = {
                    android.R.attr.navigationBarColor
            };
            final TypedArray a = getContext().obtainStyledAttributes(THEME_ATTRS);
            try {
                mNavigationBarBackground = a.getDrawable(0);
            } finally {
                a.recycle();
            }
        }
    }

    public void setNavigationBarBackground(@Nullable Drawable bg) {
        mNavigationBarBackground = bg;
        invalidate();
    }

    @Nullable
    public Drawable getNavigationBarBackgroundDrawable() {
        return mNavigationBarBackground;
    }

    public void setNavigationBarBackground(@DrawableRes int resId) {
        mNavigationBarBackground = resId != 0 ? ContextCompat.getDrawable(getContext(), resId) : null;
        invalidate();
    }

    public void setNavigationBarBackgroundColor(@ColorInt int color) {
        mNavigationBarBackground = new ColorDrawable(color);
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
            onMeasure2(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mLastInsets.getSystemWindowInsetBottom(), MeasureSpec.EXACTLY));
        } else {
            onMeasure2(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawNavigationBarBackground && mNavigationBarBackground != null) {
            final int inset;
            if (Build.VERSION.SDK_INT >= 21) {
                inset = mLastInsets != null
                        ? mLastInsets.getSystemWindowInsetBottom() : 0;
            } else {
                inset = 0;
            }
            if (inset > 0) {
                mNavigationBarBackground.setBounds(0, getHeight() - inset, getWidth(), getHeight());
                mNavigationBarBackground.draw(canvas);
            }
        }
    }
}
