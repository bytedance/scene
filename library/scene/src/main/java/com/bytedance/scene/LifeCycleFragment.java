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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scene.utlity.SceneInternalException;
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
        fragment.mCreatedByAppRestoreReflect = false;
        Bundle bundle = new Bundle();
        bundle.putBoolean(TAG_SUPPORT_RESTORE, supportRestore);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static interface LifecycleFragmentDetachCallback {
        void onDetach();
    }

    private static final String TAG = "SCENE";
    private static final String TAG_SUPPORT_RESTORE = "supportRestore";

    private FrameLayout mFragmentRootView;
    private NavigationScene mNavigationScene;
    private Scope.RootScopeFactory mRootScopeFactory;
    private boolean mSupportRestore;
    private NavigationSceneAvailableCallback mNavigationSceneAvailableCallback;
    private SceneComponentFactory mRootSceneComponentFactory;
    private final SceneLifecycleManager mLifecycleManager = new SceneLifecycleManager();
    @Nullable
    private LifecycleFragmentDetachCallback mLifecycleFragmentDetachCallback = null;
    private boolean mCreatedByAppRestoreReflect = true;
    private boolean mRemoveSelf = false;

    public void setNavigationScene(@NonNull NavigationScene rootScene, @NonNull Scope.RootScopeFactory rootScopeFactory) {
        if (rootScene == null) {
            throw new NullPointerException("rootScene can't be null");
        }
        if (rootScopeFactory == null) {
            throw new NullPointerException("rootScopeFactory can't be null");
        }
        this.mNavigationScene = rootScene;
        this.mRootScopeFactory = rootScopeFactory;
    }

    public void setRootScopeFactory(Scope.RootScopeFactory rootScopeFactory) {
        this.mRootScopeFactory = rootScopeFactory;
    }

    public void setRootSceneComponentFactory(SceneComponentFactory sceneComponentFactory) {
        this.mRootSceneComponentFactory = sceneComponentFactory;
    }

    void setLifecycleFragmentDetachCallback(@NonNull LifecycleFragmentDetachCallback lifecycleFragmentDetachCallback) {
        this.mLifecycleFragmentDetachCallback = lifecycleFragmentDetachCallback;
    }

    public NavigationScene getNavigationScene() {
        return mNavigationScene;
    }

    public void setNavigationSceneAvailableCallback(NavigationSceneAvailableCallback callback) {
        this.mNavigationSceneAvailableCallback = callback;
        if (this.mNavigationScene != null) {
            this.mNavigationSceneAvailableCallback.onNavigationSceneAvailable(this.mNavigationScene);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.mNavigationScene.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        this.mNavigationScene.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        this.mFragmentRootView = new FrameLayout(getActivity());
        return this.mFragmentRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mSupportRestore = getArguments().getBoolean(TAG_SUPPORT_RESTORE);
        if (savedInstanceState == null) {
            if (this.mNavigationScene == null) {
                if (mCreatedByAppRestoreReflect) {
                    removeSelfWhenNavigationSceneUtilityIsNotInvoked();
                } else {
                    throw new SceneInternalException("mNavigationScene can't be null");
                }
                return;
            }
        } else {
            String clazz = savedInstanceState.getString(TAG, null);
            if (clazz == null) {
                if (this.mSupportRestore) {
                    throw new SceneInternalException("LifeCycleFragment NavigationScene class name is null");
                } else {
                    removeSelfWhenNavigationSceneUtilityIsNotInvoked();
                }
                return;
            }
            this.mNavigationScene = (NavigationScene) SceneInstanceUtility.getInstanceFromClassName(getActivity(), clazz, null);
            if (this.mNavigationSceneAvailableCallback != null) {
                this.mNavigationSceneAvailableCallback.onNavigationSceneAvailable(this.mNavigationScene);
            }
        }
        this.mLifecycleManager.onActivityCreated(getActivity(), this.mFragmentRootView,
                this.mNavigationScene, this, this.mRootScopeFactory,
                this.mRootSceneComponentFactory, this.mSupportRestore ? savedInstanceState : null);
    }

    //developer may not invoke NavigationSceneUtility.setupWithActivity which will clean unused LifeCycleFragment in Activity onCreate() when app is restored,
    //for example Activity.finish()+return, at this moment, we should clean LifeCycleFragment
    private void removeSelfWhenNavigationSceneUtilityIsNotInvoked() {
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().remove(LifeCycleFragment.this);
        Utility.commitFragment(fragmentManager, fragmentTransaction, false);
        mRemoveSelf = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.mRemoveSelf) {
            return;
        }
        this.mLifecycleManager.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.mRemoveSelf) {
            return;
        }
        this.mLifecycleManager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.mRemoveSelf) {
            return;
        }
        this.mLifecycleManager.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mRemoveSelf) {
            return;
        }
        if (this.mSupportRestore) {
            outState.putString(TAG, this.mNavigationScene.getClass().getName());
            this.mLifecycleManager.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.mRemoveSelf) {
            return;
        }
        this.mLifecycleManager.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (this.mRemoveSelf) {
            return;
        }
        this.mLifecycleManager.onDestroyView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mRemoveSelf) {
            return;
        }
        this.mLifecycleManager.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (this.mRemoveSelf) {
            return;
        }
        if (this.mLifecycleFragmentDetachCallback != null) {
            this.mLifecycleFragmentDetachCallback.onDetach();
        }
    }

    @Override
    public boolean isSupportRestore() {
        return this.mSupportRestore;
    }
}
