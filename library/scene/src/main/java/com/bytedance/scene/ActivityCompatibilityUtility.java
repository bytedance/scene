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
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LifecycleOwner;
import com.bytedance.scene.interfaces.ActivityResultCallback;
import com.bytedance.scene.interfaces.PermissionResultCallback;
import com.bytedance.scene.navigation.ConfigurationChangedListener;
import com.bytedance.scene.utlity.ThreadUtility;
import com.bytedance.scene.utlity.Utility;

import static androidx.lifecycle.Lifecycle.State.DESTROYED;

//TODO remove SceneActivityCompatibilityLayerFragment when none Scene exists
public class ActivityCompatibilityUtility {
    private static SceneActivityCompatibilityLayerFragment install(@NonNull Activity activity) {
        SceneActivityCompatibilityLayerFragment instance = (SceneActivityCompatibilityLayerFragment) activity.getFragmentManager()
                .findFragmentByTag(SceneActivityCompatibilityLayerFragment.class.getName());
        if (instance == null) {
            instance = SceneActivityCompatibilityLayerFragment.newIntent();
            FragmentTransaction transaction = activity.getFragmentManager().beginTransaction().add(instance,
                    SceneActivityCompatibilityLayerFragment.class.getName());
            Utility.commitFragment(activity.getFragmentManager(), transaction, false);
        }
        return instance;
    }

    @MainThread
    public static void startActivityForResult(@NonNull final Activity activity, @NonNull final LifecycleOwner lifecycleOwner,
                                              @NonNull final Intent intent, final int requestCode,
                                              @NonNull final ActivityResultCallback resultCallback) {
        ThreadUtility.checkUIThread();
        if (isDestroyed(activity, lifecycleOwner)) {
            return;
        }
        final SceneActivityCompatibilityLayerFragment fragment = install(activity);
        if (fragment.isAdded()) {
            fragment.startActivityForResultByScene(lifecycleOwner, intent, requestCode, resultCallback);
        } else {
            fragment.addOnActivityCreatedCallback(new SceneActivityCompatibilityLayerFragment.OnActivityCreatedCallback() {
                @Override
                public void onActivityCreated() {
                    fragment.removeOnActivityCreatedCallback(this);
                    if (isDestroyed(activity, lifecycleOwner)) {
                        return;
                    }
                    fragment.startActivityForResultByScene(lifecycleOwner, intent, requestCode, resultCallback);
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @MainThread
    public static void startActivityForResult(@NonNull final Activity activity, @NonNull final LifecycleOwner lifecycleOwner,
                                              @NonNull final Intent intent, final int requestCode,
                                              @Nullable final Bundle options,
                                              @NonNull final ActivityResultCallback resultCallback) {
        ThreadUtility.checkUIThread();
        if (isDestroyed(activity, lifecycleOwner)) {
            return;
        }
        final SceneActivityCompatibilityLayerFragment fragment = install(activity);
        if (fragment.isAdded()) {
            fragment.startActivityForResultByScene(lifecycleOwner, intent, requestCode, options, resultCallback);
        } else {
            fragment.addOnActivityCreatedCallback(new SceneActivityCompatibilityLayerFragment.OnActivityCreatedCallback() {
                @Override
                public void onActivityCreated() {
                    fragment.removeOnActivityCreatedCallback(this);
                    if (isDestroyed(activity, lifecycleOwner)) {
                        return;
                    }
                    fragment.startActivityForResultByScene(lifecycleOwner, intent, requestCode, options, resultCallback);
                }
            });
        }
    }

    @MainThread
    @RequiresApi(Build.VERSION_CODES.M)
    public static void requestPermissions(@NonNull final Activity activity, @NonNull final LifecycleOwner lifecycleOwner,
                                          @NonNull final String[] permissions, final int requestCode,
                                          @NonNull final PermissionResultCallback resultCallback) {
        ThreadUtility.checkUIThread();
        if (isDestroyed(activity, lifecycleOwner)) {
            return;
        }
        final SceneActivityCompatibilityLayerFragment fragment = install(activity);
        if (fragment.isAdded()) {
            fragment.requestPermissionsByScene(lifecycleOwner, permissions, requestCode, resultCallback);
        } else {
            fragment.addOnActivityCreatedCallback(new SceneActivityCompatibilityLayerFragment.OnActivityCreatedCallback() {
                @Override
                public void onActivityCreated() {
                    fragment.removeOnActivityCreatedCallback(this);
                    if (isDestroyed(activity, lifecycleOwner)) {
                        return;
                    }
                    fragment.requestPermissionsByScene(lifecycleOwner, permissions, requestCode, resultCallback);
                }
            });
        }
    }

    @MainThread
    public static void addConfigurationChangedListener(@NonNull final Activity activity, @NonNull final LifecycleOwner lifecycleOwner,
                                                       @NonNull final ConfigurationChangedListener configurationChangedListener) {
        ThreadUtility.checkUIThread();
        if (isDestroyed(activity, lifecycleOwner)) {
            return;
        }
        final SceneActivityCompatibilityLayerFragment fragment = install(activity);
        if (fragment.isAdded()) {
            fragment.addConfigurationChangedListener(lifecycleOwner, configurationChangedListener);
        } else {
            fragment.addOnActivityCreatedCallback(new SceneActivityCompatibilityLayerFragment.OnActivityCreatedCallback() {
                @Override
                public void onActivityCreated() {
                    fragment.removeOnActivityCreatedCallback(this);
                    if (isDestroyed(activity, lifecycleOwner)) {
                        return;
                    }
                    fragment.addConfigurationChangedListener(lifecycleOwner, configurationChangedListener);
                }
            });
        }
    }

    private static boolean isDestroyed(@NonNull Activity activity, @NonNull final LifecycleOwner lifecycleOwner) {
        if (!Utility.isActivityStatusValid(activity)) {
            return true;
        }

        return lifecycleOwner.getLifecycle().getCurrentState() == DESTROYED;
    }
}
