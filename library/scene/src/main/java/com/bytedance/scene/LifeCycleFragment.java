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

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.Utility;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 7/30/18.
 *
 * @hide
 **/
@RestrictTo(LIBRARY_GROUP)
public class LifeCycleFragment extends Fragment implements NavigationScene.NavigationSceneHost {
    public static LifeCycleFragment newInstance(boolean supportRestore) {
        LifeCycleFragment fragment = new LifeCycleFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(TAG_SUPPORT_RESTORE, supportRestore);
        fragment.setArguments(bundle);
        return fragment;
    }

    private static final String TAG_SUPPORT_RESTORE = "supportRestore";

    public static interface LifecycleFragmentDetachCallback {
        void onDetach();
    }

    private boolean mSupportRestore;
    @Nullable
    private LifecycleFragmentDetachCallback mLifecycleFragmentDetachCallback = null;
    private SceneContainerLifecycleCallback mSceneContainerLifecycleCallback = null;

    void setLifecycleFragmentDetachCallback(@NonNull LifecycleFragmentDetachCallback lifecycleFragmentDetachCallback) {
        this.mLifecycleFragmentDetachCallback = lifecycleFragmentDetachCallback;
    }

    public void setSceneContainerLifecycleCallback(SceneContainerLifecycleCallback callback) {
        this.mSceneContainerLifecycleCallback = callback;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.mSceneContainerLifecycleCallback != null) {
            this.mSceneContainerLifecycleCallback.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (this.mSceneContainerLifecycleCallback != null) {
            this.mSceneContainerLifecycleCallback.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mSupportRestore = getArguments().getBoolean(TAG_SUPPORT_RESTORE);
        if (this.mSceneContainerLifecycleCallback != null) {
            this.mSceneContainerLifecycleCallback.onActivityCreated(getActivity(), savedInstanceState);
        } else {
            removeSelfWhenNavigationSceneUtilityIsNotInvoked();
        }
    }

    //developer may not invoke NavigationSceneUtility.setupWithActivity which will clean unused LifeCycleFragment in Activity onCreate() when app is restored,
    //for example Activity.finish()+return, at this moment, we should clean LifeCycleFragment
    private void removeSelfWhenNavigationSceneUtilityIsNotInvoked() {
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().remove(LifeCycleFragment.this);
        Utility.commitFragment(fragmentManager, fragmentTransaction, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.mSceneContainerLifecycleCallback != null) {
            this.mSceneContainerLifecycleCallback.onStarted();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.mSceneContainerLifecycleCallback != null) {
            this.mSceneContainerLifecycleCallback.onResumed();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.mSceneContainerLifecycleCallback != null) {
            this.mSceneContainerLifecycleCallback.onPaused();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mSceneContainerLifecycleCallback != null) {
            this.mSceneContainerLifecycleCallback.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.mSceneContainerLifecycleCallback != null) {
            this.mSceneContainerLifecycleCallback.onStopped();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (this.mSceneContainerLifecycleCallback != null) {
            this.mSceneContainerLifecycleCallback.onViewDestroyed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mSceneContainerLifecycleCallback != null) {
            this.mSceneContainerLifecycleCallback.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (this.mLifecycleFragmentDetachCallback != null) {
            this.mLifecycleFragmentDetachCallback.onDetach();
        }
    }

    @Override
    public boolean isSupportRestore() {
        return this.mSupportRestore;
    }
}
