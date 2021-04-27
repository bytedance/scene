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

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Created by JiangQi on 8/9/18.
 */
public class SlidePercentFrameLayout extends FrameLayout {
    public static interface Callback {

        boolean isSupport();

        void onStart();

        void onFinish();

        void onProgress(float progress);
    }

    private static final int EDGE_SIZE = 50; // dp
    private boolean mSwipeEnabled = true;
    private Callback mCallback;
    private int mEdgeSize;
    private float mInitRawX;
    private float mInterceptInitX;
    private int mLastMotionX;
    private int mTouchSlop;
    private boolean mIsBeingDragged = false;
    private int mActivePointerId = INVALID_POINTER;
    private static final int INVALID_POINTER = -1;
    private boolean mSkipUntilNextGestureEvent = false;

    public SlidePercentFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public SlidePercentFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlidePercentFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SlidePercentFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        final ViewConfiguration vc = ViewConfiguration.get(getContext());
        final float density = getContext().getResources().getDisplayMetrics().density;
        mEdgeSize = (int) (EDGE_SIZE * density + 0.5f);
        mTouchSlop = vc.getScaledTouchSlop();
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.mCallback == null || !this.mSwipeEnabled) {
            return super.onInterceptTouchEvent(ev);
        }

        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            return true;
        }

        if (super.onInterceptTouchEvent(ev)) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if (!mCallback.isSupport()) {
                    mSkipUntilNextGestureEvent = true;
                    return false;
                }
                final int x = (int) ev.getX();
                mLastMotionX = x;
                mInterceptInitX = x;
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mInterceptInitX > mEdgeSize || mSkipUntilNextGestureEvent) {
                    return false;
                }

                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    break;
                }

                final int x = (int) ev.getX(pointerIndex);
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                if (xDiff > mTouchSlop) {
                    mLastMotionX = x;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    startDragIfNeeded(ev);
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                finishSwipeGesture();
                break;
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.mCallback == null || this.mSkipUntilNextGestureEvent || !this.mSwipeEnabled) {
            return super.onTouchEvent(event);
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                if (!this.mIsBeingDragged && mInterceptInitX < mEdgeSize) {
                    final int activePointerId = mActivePointerId;
                    if (activePointerId == INVALID_POINTER) {
                        break;
                    }

                    final int pointerIndex = event.findPointerIndex(activePointerId);
                    if (pointerIndex == -1) {
                        break;
                    }

                    final int x = (int) event.getX(pointerIndex);
                    final int xDiff = (int) Math.abs(x - mLastMotionX);
                    if (xDiff > mTouchSlop) {
                        mLastMotionX = x;
                        getParent().requestDisallowInterceptTouchEvent(true);
                        startDragIfNeeded(event);
                    }
                }

                if (this.mIsBeingDragged) {
                    this.mCallback.onProgress(Math.max(0, event.getRawX() - this.mInitRawX) / ((float) getWidth() - this.mInitRawX));
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                finishSwipeGesture();
                break;

        }
        return true;
    }

    private void finishSwipeGesture() {
        if (this.mIsBeingDragged) {
            this.mCallback.onFinish();
            this.mIsBeingDragged = false;
        }
        this.mActivePointerId = INVALID_POINTER;
        this.mSkipUntilNextGestureEvent = false;
    }

    private void startDragIfNeeded(MotionEvent event) {
        if (this.mIsBeingDragged) {
            return;
        }
        this.mIsBeingDragged = true;
        this.mCallback.onStart();
        this.mInitRawX = event.getRawX();
    }

    public void setEdgeSize(int edgeSize) {
        this.mEdgeSize = edgeSize;
    }

    public void setSwipeEnabled(boolean enabled) {
        if (this.mSwipeEnabled == enabled) {
            return;
        }
        this.mSwipeEnabled = enabled;
        if (!this.mSwipeEnabled && this.mIsBeingDragged) {
            finishSwipeGesture();
        }
    }
}
