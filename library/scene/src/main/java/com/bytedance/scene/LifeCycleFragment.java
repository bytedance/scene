package com.bytedance.scene;

import android.app.Fragment;
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

    private static final String TAG = "SCENE";
    private static final String TAG_SUPPORT_RESTORE = "supportRestore";

    private FrameLayout mFragmentRootView;
    private NavigationScene mNavigationScene;
    private Scope.RootScopeFactory mRootScopeFactory;
    private boolean mSupportRestore;
    private NavigationSceneAvailableCallback mNavigationSceneAvailableCallback;
    private SceneComponentFactory mRootSceneComponentFactory;
    private final SceneLifecycleManager mLifecycleManager = new SceneLifecycleManager();

    public void setNavigationScene(NavigationScene rootScene, Scope.RootScopeFactory rootScopeFactory) {
        this.mNavigationScene = rootScene;
        this.mRootScopeFactory = rootScopeFactory;
    }

    public void setRootScopeFactory(Scope.RootScopeFactory rootScopeFactory) {
        this.mRootScopeFactory = rootScopeFactory;
    }

    public void setRootSceneComponentFactory(SceneComponentFactory sceneComponentFactory) {
        this.mRootSceneComponentFactory = sceneComponentFactory;
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
                throw new IllegalStateException("mNavigationScene can't be null");
            }
        } else {
            String clazz = savedInstanceState.getString(TAG);
            this.mNavigationScene = (NavigationScene) SceneInstanceUtility.getInstanceFromClassName(getActivity(), clazz, null);
            if (this.mNavigationSceneAvailableCallback != null) {
                this.mNavigationSceneAvailableCallback.onNavigationSceneAvailable(this.mNavigationScene);
            }
        }
        this.mLifecycleManager.onActivityCreated(getActivity(), this.mFragmentRootView,
                this.mNavigationScene, this, this.mRootScopeFactory,
                this.mRootSceneComponentFactory, this.mSupportRestore ? savedInstanceState : null);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.mLifecycleManager.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mLifecycleManager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mLifecycleManager.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mSupportRestore) {
            outState.putString(TAG, this.mNavigationScene.getClass().getName());
            this.mLifecycleManager.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        this.mLifecycleManager.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.mLifecycleManager.onDestroyView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mLifecycleManager.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean isSupportRestore() {
        return this.mSupportRestore;
    }
}
