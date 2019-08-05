package com.bytedance.scene.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.scene.*;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scene.utlity.SceneInternalException;

public class LifeCycleCompatFragment extends Fragment implements NavigationScene.NavigationSceneHost {
    public static LifeCycleCompatFragment newInstance(boolean supportRestore) {
        LifeCycleCompatFragment fragment = new LifeCycleCompatFragment();
        fragment.mCreatedByAppRestoreReflect = false;
        Bundle bundle = new Bundle();
        bundle.putBoolean(TAG_SUPPORT_RESTORE, supportRestore);
        fragment.setArguments(bundle);
        return fragment;
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
    private boolean mCreatedByAppRestoreReflect = true;
    private boolean mRemoveSelf = false;

    public void setNavigationScene(NavigationScene rootScene, Scope.RootScopeFactory rootScopeFactory) {
        this.mNavigationScene = rootScene;
        this.mRootScopeFactory = rootScopeFactory;
    }

    public void setRootScopeFactory(Scope.RootScopeFactory rootScopeFactory) {
        this.mRootScopeFactory = rootScopeFactory;
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

    public void setRootSceneComponentFactory(SceneComponentFactory sceneComponentFactory) {
        this.mRootSceneComponentFactory = sceneComponentFactory;
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
                    throw new SceneInternalException("LifeCycleCompatFragment NavigationScene class name is null");
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

    //developer may not invoke NavigationSceneCompatUtility.setupWithFragment which will clean unused LifeCycleCompatFragment when app is restored,
    //for example Activity.finish()+return, at this moment, we should clean LifeCycleCompatFragment
    private void removeSelfWhenNavigationSceneUtilityIsNotInvoked() {
        getFragmentManager().beginTransaction().remove(LifeCycleCompatFragment.this).commitAllowingStateLoss();
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
    public boolean isSupportRestore() {
        return this.mSupportRestore;
    }
}