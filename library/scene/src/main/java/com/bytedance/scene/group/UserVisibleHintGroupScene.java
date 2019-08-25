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

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 8/23/18.
 */
public abstract class UserVisibleHintGroupScene extends GroupScene {
    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public static final String KEY_SCENE_USER_VISIBLE_HINT = "bd-scene-nav:scene_user_visible_hint";

    private LifecycleRegistry mUserVisibleHintLifecycleRegistry = new LifecycleRegistry(new LifecycleOwner() {
        @NonNull
        @Override
        public Lifecycle getLifecycle() {
            return mUserVisibleHintLifecycleRegistry;
        }
    });

    private boolean mUserVisibleHint = true;
    private boolean mResume = false;
    private boolean mStart = false;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SCENE_USER_VISIBLE_HINT, this.mUserVisibleHint);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.mUserVisibleHint = savedInstanceState.getBoolean(KEY_SCENE_USER_VISIBLE_HINT);
        }
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (this.mUserVisibleHint == isVisibleToUser) {
            return;
        }
        mUserVisibleHint = isVisibleToUser;
        dispatchVisibleChanged();

        if (mUserVisibleHint) {
            if (mStart) {
                mUserVisibleHintLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
            }
            if (mResume) {
                mUserVisibleHintLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
            }
        } else {
            if (mResume) {
                mUserVisibleHintLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
            }

            if (mStart) {
                mUserVisibleHintLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUserVisibleHintLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

        getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            void onPause() {
                mResume = false;
                if (mUserVisibleHint) {
                    mUserVisibleHintLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            void onResume() {
                mResume = true;
                if (mUserVisibleHint) {
                    mUserVisibleHintLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            void onStop() {
                mStart = false;
                if (mUserVisibleHint) {
                    mUserVisibleHintLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            void onStart() {
                mStart = true;
                if (mUserVisibleHint) {
                    mUserVisibleHintLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            void onDestroy() {
                mUserVisibleHintLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
            }
        });
    }

    public boolean getUserVisibleHint() {
        return mUserVisibleHint;
    }

    public Lifecycle getUserVisibleHintLifecycle() {
        return this.mUserVisibleHintLifecycleRegistry;
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && mUserVisibleHint;
    }
}
