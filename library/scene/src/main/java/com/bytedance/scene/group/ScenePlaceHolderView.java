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
package com.bytedance.scene.group;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import com.bytedance.scene.R;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.utlity.Utility;

public final class ScenePlaceHolderView extends View {
    private String mSceneName;
    private String mSceneTag;
    private Bundle mSceneArguments;
    private SceneComponentFactory mSceneComponentFactory;

    public ScenePlaceHolderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScenePlaceHolderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScenePlaceHolderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ScenePlaceHolderView,
                0, 0);
        try {
            mSceneName = typedArray.getString(R.styleable.ScenePlaceHolderView_name);
            mSceneTag = typedArray.getString(R.styleable.ScenePlaceHolderView_tag);
        } finally {
            typedArray.recycle();
        }
    }

    public void setSceneComponentFactory(@Nullable SceneComponentFactory callback) {
        this.mSceneComponentFactory = callback;
    }

    @Nullable
    public SceneComponentFactory getSceneComponentFactory() {
        return this.mSceneComponentFactory;
    }

    public void setSceneTag(@NonNull String tag) {
        this.mSceneTag = Utility.requireNonEmpty(tag, "ScenePlaceHolderView tag can't be empty");
    }

    @NonNull
    public String getSceneTag() {
        if (TextUtils.isEmpty(mSceneTag)) {
            throw new IllegalArgumentException("ScenePlaceHolderView tag is empty, invoke setSceneTag first");
        }
        return this.mSceneTag;
    }

    public void setSceneName(@NonNull String name) {
        this.mSceneName = Utility.requireNonEmpty(name, "ScenePlaceHolderView name can't be empty");
    }

    @NonNull
    public String getSceneName() {
        if (TextUtils.isEmpty(mSceneName)) {
            throw new IllegalArgumentException("ScenePlaceHolderView name is empty, invoke setSceneName first");
        }
        return this.mSceneName;
    }

    public void setArguments(@Nullable Bundle bundle) {
        this.mSceneArguments = bundle;
    }

    @Nullable
    public Bundle getArguments() {
        return this.mSceneArguments;
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                getDefaultSize2(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize2(getSuggestedMinimumHeight(), heightMeasureSpec));
    }
}
