/*
 * Copyright (C) 2019 ByteDance Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bytedance.scene.view;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/**
 * Created by JiangQi on 7/30/18.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class NavigationFrameLayout extends FrameLayout {
    private boolean mIsTouchEnabled = true;

    private boolean mSupportRestore;

    private boolean mDrawAnimationViewToFront = false;
    private boolean mAnimationContainerViewAdded = false;

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

    /**
     * The Scene framework is responsible for the state preservation of the View,
     * avoiding the state save exceptions of multiple Scenes in the same type, as the root layout id is the same
     */
    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    public void setChildrenDrawingOrderEnabled(boolean enabled) {
        super.setChildrenDrawingOrderEnabled(enabled);
    }

    public void setDrawAnimationViewToFront(boolean value) {
        if (!isChildrenDrawingOrderEnabled()) {
            throw new IllegalStateException("please invoke setChildrenDrawingOrderEnabled(true) first");
        }
        this.mDrawAnimationViewToFront = value;
        this.invalidate();
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int drawingPosition) {
        if (this.mDrawAnimationViewToFront && this.mAnimationContainerViewAdded) {
            //AnimationContainerLayout should always located in first position
            if (drawingPosition == childCount - 1) {
                return 0;
            } else {
                return drawingPosition + 1;
            }
        } else {
            return super.getChildDrawingOrder(childCount, drawingPosition);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (!isChildrenDrawingOrderEnabled()) {
            return;
        }

        Class<? extends View> childClass = child.getClass();
        //skip to load AnimationContainerLayout class to improve startup performance
        boolean isSystemFrameworkView = childClass == FrameLayout.class || childClass == LinearLayout.class || childClass == RelativeLayout.class;

        if (!isSystemFrameworkView && child instanceof AnimationContainerLayout) {
            if (index != 0) {
                throw new IllegalArgumentException("AnimationContainerLayout should add to 0");
            }
            if (this.mAnimationContainerViewAdded) {
                throw new IllegalStateException("AnimationContainerLayout is already added");
            } else {
                this.mAnimationContainerViewAdded = true;
            }
        } else {
            if (this.mAnimationContainerViewAdded && index == 0) {
                throw new IllegalArgumentException("only AnimationContainerLayout can be added to 0");
            }
        }
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (!isChildrenDrawingOrderEnabled()) {
            return;
        }
        if (mAnimationContainerViewAdded && (child instanceof AnimationContainerLayout)) {
            this.mAnimationContainerViewAdded = false;
        }
    }
}
