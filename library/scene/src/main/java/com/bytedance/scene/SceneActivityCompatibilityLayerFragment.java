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
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.*;
import androidx.collection.SparseArrayCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import com.bytedance.scene.interfaces.ActivityResultCallback;
import com.bytedance.scene.interfaces.PermissionResultCallback;
import com.bytedance.scene.navigation.ConfigurationChangedListener;
import com.bytedance.scene.utlity.ThreadUtility;
import com.bytedance.scene.utlity.Utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.lifecycle.Lifecycle.State.DESTROYED;

/**
 * @hide
 **/
@RestrictTo(LIBRARY_GROUP)
public class SceneActivityCompatibilityLayerFragment extends Fragment {
    public interface OnActivityCreatedCallback {
        void onActivityCreated();
    }

    private final SparseArrayCompat<ActivityResultCallback> mResultCallbackMap = new SparseArrayCompat<>();
    private final SparseArrayCompat<PermissionResultCallback> mPermissionResultCallbackMap = new SparseArrayCompat<>();
    private final List<ConfigurationChangedListener> mConfigurationChangedListenerList = new ArrayList<>();
    private final Set<OnActivityCreatedCallback> mOnActivityCreatedCallbackSet = new HashSet<>();

    static SceneActivityCompatibilityLayerFragment newIntent() {
        return new SceneActivityCompatibilityLayerFragment();
    }

    void addOnActivityCreatedCallback(@Nullable OnActivityCreatedCallback callback) {
        this.mOnActivityCreatedCallbackSet.add(callback);
    }

    void removeOnActivityCreatedCallback(@Nullable OnActivityCreatedCallback callback) {
        this.mOnActivityCreatedCallbackSet.remove(callback);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Set<OnActivityCreatedCallback> copy = new HashSet<>(this.mOnActivityCreatedCallbackSet);
        for (OnActivityCreatedCallback callback : copy) {
            callback.onActivityCreated();
        }
    }

    @MainThread
    void startActivityForResultByScene(@NonNull final LifecycleOwner lifecycleOwner, @NonNull Intent intent, final int requestCode, @NonNull ActivityResultCallback resultCallback) {
        if (!isCurrentStatusValid(lifecycleOwner)) {
            return;
        }

        if (requestCode < 0) {
            startActivity(intent);
            return;
        }
        mResultCallbackMap.put(requestCode, resultCallback);
        startActivityForResult(intent, requestCode);
        lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            void onDestroy() {
                lifecycleOwner.getLifecycle().removeObserver(this);
                mResultCallbackMap.remove(requestCode);
            }
        });
    }

    @MainThread
    @RequiresApi(Build.VERSION_CODES.M)
    void requestPermissionsByScene(@NonNull final LifecycleOwner lifecycleOwner, @NonNull String[] permissions, final int requestCode, @NonNull final PermissionResultCallback resultCallback) {
        if (!isCurrentStatusValid(lifecycleOwner)) {
            return;
        }
        mPermissionResultCallbackMap.put(requestCode, resultCallback);
        requestPermissions(permissions, requestCode);
        lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            void onDestroy() {
                lifecycleOwner.getLifecycle().removeObserver(this);
                mPermissionResultCallbackMap.remove(requestCode);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultCallback callback = mResultCallbackMap.get(requestCode);
        if (callback != null) {
            callback.onResult(resultCode, data);
            mResultCallbackMap.remove(requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionResultCallback callback = mPermissionResultCallbackMap.get(requestCode);
        if (callback != null) {
            callback.onResult(grantResults);
            mPermissionResultCallbackMap.remove(requestCode);
        }
    }

    private boolean isCurrentStatusValid(@NonNull final LifecycleOwner lifecycleOwner) {
        ThreadUtility.checkUIThread();
        Activity activity = getActivity();
        if (!Utility.isActivityStatusValid(activity)) {
            return false;
        }

        if (lifecycleOwner.getLifecycle().getCurrentState() == DESTROYED) {
            // ignore
            return false;
        }
        return true;
    }

    @MainThread
    void addConfigurationChangedListener(@NonNull final LifecycleOwner lifecycleOwner, @NonNull final ConfigurationChangedListener configurationChangedListener) {
        if (!isCurrentStatusValid(lifecycleOwner)) {
            return;
        }
        mConfigurationChangedListenerList.add(configurationChangedListener);
        lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            void onDestroy() {
                lifecycleOwner.getLifecycle().removeObserver(this);
                mConfigurationChangedListenerList.remove(configurationChangedListener);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        List<ConfigurationChangedListener> copy = new ArrayList<>(mConfigurationChangedListenerList);
        for (int i = copy.size() - 1; i >= 0; i--) {
            ConfigurationChangedListener listener = copy.get(i);
            if (listener != null) {
                listener.onConfigurationChanged(newConfig);
            }
        }
    }
}
