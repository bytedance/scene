package com.bytedance.scene.view;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.AttributeSet;
import android.util.SparseArray;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 7/30/18.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class NavigationFrameLayout extends NoneTouchFrameLayout {
    private boolean mSupportRestore;

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

    private boolean isSupportRestore() {
        return this.mSupportRestore;
    }

    public void setSupportRestore(boolean value) {
        this.mSupportRestore = value;
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        if (isSupportRestore()) {
            super.dispatchSaveInstanceState(container);
        }
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        if (isSupportRestore()) {
            super.dispatchRestoreInstanceState(container);
        }
    }
}
