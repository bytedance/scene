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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.State;

/**
 * Created by JiangQi on 9/19/18.
 * Need to provide a set of LifeCycle
 */
public abstract class AsyncLayoutGroupScene extends UserVisibleHintGroupScene {
    private boolean mAsyncLayoutEnabled = false;
    private boolean mViewAsyncCreated = false;

    @NonNull
    @Override
    public final ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        if (this.mAsyncLayoutEnabled) {
            final FrameLayout frameLayout = new FrameLayout(requireSceneContext());
            SceneAsyncLayoutInflater asyncLayoutInflater = new SceneAsyncLayoutInflater(requireSceneContext());
            asyncLayoutInflater.inflate(getLayoutId(), container, new SceneAsyncLayoutInflater.OnInflateFinishedListener() {
                @Override
                public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                    State state = getState();
                    if (state == State.NONE) {
                        return;
                    }

                    if (state.value >= State.VIEW_CREATED.value) {
                        frameLayout.addView(view);
                        mViewAsyncCreated = true;
                        onAsyncViewCreated(getView(), savedInstanceState);
                    }

                    if (state.value >= State.ACTIVITY_CREATED.value) {
                        onAsyncActivityCreated(savedInstanceState);
                    }

                    if (state.value >= State.STARTED.value) {
                        onAsyncStart();
                    }

                    if (state.value == State.RESUMED.value) {
                        onAsyncResume();
                    }
                }
            });
            return frameLayout;
        } else {
            View view = inflater.inflate(getLayoutId(), container, false);
            if (!(view instanceof ViewGroup)) {
                throw new IllegalArgumentException("AsyncLayoutGroupScene getLayoutId() view must be ViewGroup");
            }
            this.mViewAsyncCreated = true;
            return (ViewGroup) view;
        }
    }

    @LayoutRes
    protected abstract int getLayoutId();

    public boolean isViewAsyncCreated() {
        return this.mViewAsyncCreated;
    }

    public void setAsyncLayoutEnabled(boolean value) {
        this.mAsyncLayoutEnabled = value;
    }

    public boolean isAsyncLayoutEnabled() {
        return this.mAsyncLayoutEnabled;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            setAsyncLayoutEnabled(false);
        } else {
            setAsyncLayoutEnabled(true);
        }
    }

    @Override
    public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isViewAsyncCreated()) {
            onAsyncViewCreated(view, savedInstanceState);
        }
    }

    @Override
    public final void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isViewAsyncCreated()) {
            onAsyncActivityCreated(savedInstanceState);
        }
    }

    @Override
    public final void onStart() {
        super.onStart();
        if (isViewAsyncCreated()) {
            onAsyncStart();
        }
    }

    @Override
    public final void onResume() {
        super.onResume();
        if (isViewAsyncCreated()) {
            onAsyncResume();
        }
    }

    @Override
    public final void onPause() {
        super.onPause();
        if (isViewAsyncCreated()) {
            onAsyncPause();
        }
    }

    @Override
    public final void onStop() {
        super.onStop();
        if (isViewAsyncCreated()) {
            onAsyncStop();
        }
    }

    @CallSuper
    public void onAsyncViewCreated(View view, @Nullable Bundle savedInstanceState) {

    }

    @CallSuper
    public void onAsyncActivityCreated(Bundle savedInstanceState) {

    }

    @CallSuper
    public void onAsyncStart() {

    }

    @CallSuper
    public void onAsyncResume() {

    }

    @CallSuper
    public void onAsyncPause() {

    }

    @CallSuper
    public void onAsyncStop() {

    }
}
