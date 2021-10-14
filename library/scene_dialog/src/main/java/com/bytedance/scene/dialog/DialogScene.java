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
package com.bytedance.scene.dialog;

import android.app.Activity;
import android.os.Build;
import com.bytedance.scene.navigation.NavigationSceneGetter;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.animatorexecutor.DialogSceneAnimatorExecutor;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.SceneTranslucent;
import com.bytedance.scene.utlity.Experimental;

/**
 * Created by JiangQi on 8/2/18.
 */
@Experimental
public abstract class DialogScene extends Scene implements SceneTranslucent {

    public void show(@NonNull Scene hostScene) {
        if (hostScene.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            return;
        }
        Activity activity = hostScene.getActivity();
        if (activity == null) {
            return;
        }

        if (activity.isFinishing()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
            return;
        }

        NavigationScene navigationScene = NavigationSceneGetter.getNavigationScene(hostScene);
        if (navigationScene == null) {
            return;
        }

        navigationScene.push(this);
    }
}