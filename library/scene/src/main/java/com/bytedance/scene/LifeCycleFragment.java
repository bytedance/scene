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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.Utility;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    /**
     * If framework Fragment don't have View, getFragmentManager().beginTransaction().show(fragment).commitNowAllowingStateLoss()
     * may throw NPE in Android 8.1.0
     *
     * java.lang.NullPointerException: Attempt to invoke virtual method 'void android.view.View.setVisibility(int)' on a null object reference
     *         at android.app.FragmentTransition.configureSharedElementsReordered(FragmentTransition.java:487)
     *         at android.app.FragmentTransition.configureTransitionsReordered(FragmentTransition.java:212)
     *         at android.app.FragmentTransition.startTransitions(FragmentTransition.java:116)
     *         at android.app.FragmentManagerImpl.executeOpsTogether(FragmentManager.java:2194)
     *         at android.app.FragmentManagerImpl.removeRedundantOperationsAndExecute(FragmentManager.java:2142)
     *         at android.app.FragmentManagerImpl.execSingleAction(FragmentManager.java:2013)
     *         at android.app.BackStackRecord.commitNowAllowingStateLoss(BackStackRecord.java:662)
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(getActivity());
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
