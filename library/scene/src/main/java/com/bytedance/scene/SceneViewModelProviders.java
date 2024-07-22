

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

import android.app.Activity;
import android.app.Application;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.bytedance.scene.utlity.ThreadUtility;

public class SceneViewModelProviders {
    private SceneViewModelProviders() {

    }

    private static Application checkApplication(Activity activity) {
        Application application = activity.getApplication();
        if (application == null) {
            throw new IllegalStateException("Your activity is not yet attached to "
                    + "Application. You can't request ViewModel before onCreate call.");
        }
        return application;
    }

    private static Activity checkActivity(Scene scene) {
        Activity activity = scene.getActivity();
        if (activity == null) {
            throw new IllegalStateException("Can't create ViewModelProvider for removed scene");
        }
        return activity;
    }

    @MainThread
    public static ViewModelProvider of(@NonNull Scene scene) {
        if (SceneGlobalConfig.validateSceneViewModelProvidersMainThreadStrategy) {
            ThreadUtility.checkUIThread();
        }

        ViewModelProvider.AndroidViewModelFactory factory =
                ViewModelProvider.AndroidViewModelFactory.getInstance(
                        checkApplication(checkActivity(scene)));
        return new ViewModelProvider(scene.getViewModelStore(), factory);
    }

    @MainThread
    public static ViewModelProvider of(@NonNull Scene scene, @NonNull ViewModelProvider.Factory factory) {
        if (SceneGlobalConfig.validateSceneViewModelProvidersMainThreadStrategy) {
            ThreadUtility.checkUIThread();
        }
        return new ViewModelProvider(scene.getViewModelStore(), factory);
    }
}
