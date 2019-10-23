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
package com.bytedance.scene;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.bytedance.scene.navigation.NavigationScene;

/**
 * Created by JiangQi on 11/6/18.
 */
public abstract class LifeCycleFrameLayout extends FrameLayout implements NavigationScene.NavigationSceneHost {
    private static final boolean DEBUG = false;
    private static final String TAG = "LifeCycleFrameLayout";

    public LifeCycleFrameLayout(@NonNull Context context) {
        super(context);
    }

    public LifeCycleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LifeCycleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LifeCycleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Nullable
    private NavigationScene mNavigationScene;
    @Nullable
    private SceneComponentFactory mRootSceneComponentFactory;
    @NonNull
    private Scope.RootScopeFactory mRootScopeFactory = new Scope.RootScopeFactory() {
        @Override
        public Scope getRootScope() {
            return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
        }
    };
    private final SceneLifecycleManager mLifecycleManager = new SceneLifecycleManager();

    public void setNavigationScene(@NonNull NavigationScene rootScene) {
        this.mNavigationScene = rootScene;
    }

    public void setRootSceneComponentFactory(@NonNull SceneComponentFactory rootSceneComponentFactory) {
        this.mRootSceneComponentFactory = rootSceneComponentFactory;
    }

    public void setRootScopeFactory(@NonNull Scope.RootScopeFactory rootScopeFactory) {
        this.mRootScopeFactory = rootScopeFactory;
    }

    @Nullable
    public NavigationScene getNavigationScene() {
        return mNavigationScene;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (this.mNavigationScene == null) {
            throw new NullPointerException("NavigationScene is null");
        }

        Activity activity = null;
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                activity = (Activity) context;
                break;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }

        if (activity == null) {
            throw new IllegalStateException("cant find Activity attached to this View");
        }
        this.mLifecycleManager.onActivityCreated(activity,
                this,
                this.mNavigationScene,
                this,
                this.mRootScopeFactory,
                this.mRootSceneComponentFactory,
                isSupportRestore() ? savedInstanceState : null);
    }

    public void onStart() {
        this.mLifecycleManager.onStart();
    }

    public void onResume() {
        this.mLifecycleManager.onResume();
    }

    public void onPause() {
        this.mLifecycleManager.onPause();
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (!isSupportRestore()) {
            return;
        }
        this.mLifecycleManager.onSaveInstanceState(outState);
    }

    public void onStop() {
        this.mLifecycleManager.onStop();
    }

    public void onDestroyView() {
        this.mLifecycleManager.onDestroyView();
    }

    private void log(String log) {
        if (DEBUG) {
            Log.d(TAG, log);
        }
    }
}
