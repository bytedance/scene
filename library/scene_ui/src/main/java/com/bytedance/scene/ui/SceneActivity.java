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
package com.bytedance.scene.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bytedance.scene.NavigationSceneUtility;
import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneDelegate;
import com.bytedance.scene.navigation.NavigationScene;

/**
 * Created by JiangQi on 8/24/18.
 */
public abstract class SceneActivity extends AppCompatActivity {
    private SceneDelegate mDelegate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        Bundle arguments = getHomeSceneArguments(getIntent());
        this.mDelegate = NavigationSceneUtility.setupWithActivity(this, getHomeSceneClass())
                .rootSceneArguments(arguments)
                .supportRestore(supportRestore())
                .usePostInLifecycle(getUsePostInLifecycle())
                .onlyRestoreVisibleScene(true)
                .separateCreateFromCreateView(true)
                .build();
    }

    @Override
    public void onBackPressed() {
        if (!this.mDelegate.onBackPressed()) {
            super.onBackPressed();
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        NavigationScene navigationScene = mDelegate.getNavigationScene();
        if (navigationScene != null) {
            navigationScene.onConfigurationChanged(newConfig);
        }
    }

    protected boolean getUsePostInLifecycle() {
        return true;
    }

    @NonNull
    protected abstract Class<? extends Scene> getHomeSceneClass();

    protected abstract boolean supportRestore();

    @Nullable
    protected Bundle getHomeSceneArguments(Intent intent) {
        return null;
    }
}
