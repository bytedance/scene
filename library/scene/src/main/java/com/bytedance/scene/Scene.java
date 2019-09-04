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
import android.arch.lifecycle.*;
import android.content.Context;
import android.content.res.Resources;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.parcel.ParcelConstants;
import com.bytedance.scene.utlity.SceneInternalException;
import com.bytedance.scene.utlity.Utility;
import com.bytedance.scene.view.SceneContextThemeWrapper;

import java.util.ArrayList;
import java.util.List;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 7/30/18.
 * No back stack management
 *
 * onAttach
 * onCreate
 * onCreateView
 * onViewCreated
 * onActivityCreated
 * onViewStateRestored (only when App restore)
 * onStart
 * onResume
 *
 * onPause
 * onSaveInstanceState (only when Activity or Fragment is invisible)
 * onStop
 * onDestroyView
 * onDestroy
 * onDetach
 *
 * Initial state: NONE
 *
 * When entering:
 * 1.onAttach -> onCreate -> onCreateView -> onViewCreated: then set state to VIEW_CREATED
 * 2.onActivityCreated: set state to ACTIVITY_CREATED, and set Lifecycle to Lifecycle.Event.ON_CREATE
 * 3.onStart: set state to STARTED, and set Lifecycle to Lifecycle.Event.ON_START
 * 4.onResume: set state to RESUMED, and set Lifecycle to Lifecycle.Event.ON_RESUME
 *
 * When exiting:
 * 1.onPause: set state to State成STARTED, and set Lifecycle to Lifecycle.Event.ON_PAUSE
 * 2.onStop: set state to ACTIVITY_CREATED, and set Lifecycle to Lifecycle.Event.ON_STOP
 * 3.onDestroyView: set state to NONE, and set Lifecycle to Lifecycle.Event.ON_DESTROY
 * 4.onDestroy -> onDetach
 */
public abstract class Scene implements LifecycleOwner, ViewModelStoreOwner {
    public static final String SCENE_SERVICE = "scene";
    private static final String TAG = "Scene";

    private Activity mActivity;
    private Context mSceneContext;
    private LayoutInflater mLayoutInflater;
    private View mView;

    private Scene mParentScene;
    private Scope.RootScopeFactory mRootScopeFactory = Scope.DEFAULT_ROOT_SCOPE_FACTORY;
    private Scope mScope;
    private NavigationScene mNavigationScene;
    private State mState = State.NONE;
    private StringBuilder mStateHistoryBuilder = new StringBuilder(mState.name);
    private Bundle mArguments;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private List<Runnable> mPendingActionList = new ArrayList<>();
    private boolean mCalled = false;
    private boolean mVisibleDispatched = false;

    private int mThemeResource;

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @NonNull
    public final State getState() {
        return this.mState;
    }

    private void setState(@NonNull State state) {
        State currentState = this.mState;
        if (state.value > currentState.value) {
            //when Scene enter, state +1
            if ((state.value - currentState.value) != 1) {
                throw new SceneInternalException("Cant setState from " + currentState.name + " to " + state.name);
            }
        } else if (state.value < currentState.value) {
            //when Scene exit, state -1, except for from State.ACTIVITY_CREATED to State.NONE
            if (currentState == State.ACTIVITY_CREATED && state == State.NONE) {
                //empty
            } else if (state.value - currentState.value != -1) {
                throw new SceneInternalException("Cant setState from " + currentState.name + " to " + state.name);
            }
        }

        this.mState = state;
        this.mStateHistoryBuilder.append(" - " + state.name);
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public final void setRootScopeFactory(@Nullable Scope.RootScopeFactory rootScopeFactory) {
        this.mRootScopeFactory = rootScopeFactory;
    }

    public final void setArguments(@NonNull Bundle arguments) {
        this.mArguments = arguments;
    }

    @Nullable
    public final Bundle getArguments() {
        return this.mArguments;
    }

    @NonNull
    public final Bundle requireArguments() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new IllegalStateException("Scene " + this + " does not have any arguments.");
        }
        return arguments;
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchAttachActivity(@NonNull Activity activity) {
        this.mActivity = activity;
        // Scene need to reset status when reuse after been destroyed
        if (this.mLifecycleRegistry.getCurrentState() != Lifecycle.State.INITIALIZED) {
            this.mLifecycleRegistry.rest();
        }
    }

    private static class FixSceneReuseLifecycleAdapter extends Lifecycle {
        private final LifecycleRegistry lifecycleRegistry;
        private final List<LifecycleObserver> lifecycleObservers = new ArrayList<>();

        private FixSceneReuseLifecycleAdapter(LifecycleRegistry lifecycleRegistry) {
            this.lifecycleRegistry = lifecycleRegistry;
        }

        @Override
        public void addObserver(@NonNull LifecycleObserver observer) {
            this.lifecycleObservers.add(observer);
            this.lifecycleRegistry.addObserver(observer);
        }

        @Override
        public void removeObserver(@NonNull LifecycleObserver observer) {
            this.lifecycleObservers.remove(observer);
            this.lifecycleRegistry.removeObserver(observer);
        }

        @NonNull
        @Override
        public State getCurrentState() {
            return this.lifecycleRegistry.getCurrentState();
        }

        void handleLifecycleEvent(@NonNull Lifecycle.Event event) {
            this.lifecycleRegistry.handleLifecycleEvent(event);
        }

        void rest() {
            // Otherwise it will loop endless
            for (LifecycleObserver lifecycleObserver : lifecycleObservers) {
                this.lifecycleRegistry.removeObserver(lifecycleObserver);
            }
            this.lifecycleRegistry.markState(Lifecycle.State.INITIALIZED);
            for (LifecycleObserver lifecycleObserver : lifecycleObservers) {
                this.lifecycleRegistry.addObserver(lifecycleObserver);
            }
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchAttachScene(@Nullable Scene parentScene) {
        if (parentScene != null) {
            this.mParentScene = parentScene;
            if (this.mParentScene instanceof NavigationScene) {
                this.mNavigationScene = (NavigationScene) this.mParentScene;
            } else {
                this.mNavigationScene = this.mParentScene.getNavigationScene();
            }
        }
        mCalled = false;
        onAttach();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onAttach()");
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchCreate(@Nullable Bundle savedInstanceState) {
        if (mParentScene == null) {
            mScope = mRootScopeFactory.getRootScope();
        } else {
            mScope = mParentScene.getScope().buildScope(this, savedInstanceState);
        }
        if (savedInstanceState != null) {
            boolean hasArgument = savedInstanceState.getBoolean(ParcelConstants.KEY_SCENE_HAS_ARGUMENT);
            if (hasArgument) {
                Bundle bundle = savedInstanceState.getBundle(ParcelConstants.KEY_SCENE_ARGUMENT);
                if (bundle == null) {
                    throw new IllegalStateException("can't get Scene arguments from savedInstanceState");
                }
                bundle.setClassLoader(getClass().getClassLoader());
                setArguments(bundle);
            }
        }

        mCalled = false;
        onCreate(savedInstanceState);
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onCreate()");
        }
        dispatchOnSceneCreated(this, savedInstanceState, false);
    }

    @NonNull
    protected final LayoutInflater getLayoutInflater() {
        if (this.mLayoutInflater == null) {
            this.mLayoutInflater = onGetLayoutInflater();
        }
        return mLayoutInflater;
    }

    protected LayoutInflater onGetLayoutInflater() {
        if (this.mActivity == null) {
            throw new IllegalStateException("onGetLayoutInflater() cannot be executed until the "
                    + "Scene is attached to the Activity.");
        }

        return this.mActivity.getLayoutInflater().cloneInContext(requireSceneContext());
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchCreateView(@Nullable Bundle savedInstanceState, @NonNull ViewGroup container) {
        if (this.mView != null) {
            throw new IllegalArgumentException("Scene already call onCreateView");
        }

        final View view = onCreateView(getLayoutInflater(), container, savedInstanceState);
        if (view == null) {
            throw new IllegalArgumentException("onCreateView cant return null");
        }

        if (view.getParent() != null) {
            throw new IllegalArgumentException("onCreateView return view already has a parent. You must call removeView() on the view's parent first.");
        }

        if (view.getId() == View.NO_ID) {

        }

        mView = view;
        mCalled = false;
        onViewCreated(mView, savedInstanceState);
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onViewCreated()");
        }
        setState(State.VIEW_CREATED);
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchActivityCreated(@Nullable Bundle savedInstanceState) {
        mCalled = false;
        onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            dispatchViewStateRestored(savedInstanceState);
        }
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onActivityCreated()");
        }
        setState(State.ACTIVITY_CREATED);
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchStart() {
        mCalled = false;
        onStart();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onStart()");
        }
        setState(State.STARTED);
        dispatchVisibleChanged();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
        dispatchOnSceneStarted(this, false);
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    private void dispatchViewStateRestored(@NonNull Bundle savedInstanceState) {
        mCalled = false;
        onViewStateRestored(savedInstanceState);
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onViewStateRestored()");
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchResume() {
        mCalled = false;
        onResume();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onResume()");
        }
        setState(State.RESUMED);
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
        dispatchOnSceneResumed(this, false);
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchPause() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
        setState(State.STARTED);
        mCalled = false;
        onPause();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onPause()");
        }
        dispatchOnScenePaused(this, false);
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchSaveInstanceState(Bundle outState) {
        mCalled = false;
        onSaveInstanceState(outState);
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onSaveInstanceState()");
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchStop() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
        setState(State.ACTIVITY_CREATED);
        mCalled = false;
        onStop();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onStop()");
        }
        dispatchOnSceneStopped(this, false);
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchDestroyView() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        setState(State.NONE);
        mCalled = false;
        onDestroyView();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onDestroyView()");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.mView.cancelPendingInputEvents();
        }
        this.mView = null;
        this.mLayoutInflater = null;
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchDestroy() {
        mCalled = false;
        onDestroy();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onDestroy()");
        }
        dispatchOnSceneDestroyed(this, false);
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchDetachScene() {
        this.mParentScene = null;
        this.mNavigationScene = null;
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchDetachActivity() {
        Activity activity = this.mActivity;
        this.mActivity = null;
        this.mSceneContext = null;
        mCalled = false;
        onDetach();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onDetach()");
        }

        if (!activity.isChangingConfigurations()) {
            this.mScope.destroy();
        }
        this.mScope = null;
        // Must be called last, in case someone do add in onDestroy/onDetach
        mPendingActionList.clear();
    }

    /**
     * use {@link #onActivityCreated(Bundle)} instead
     */
    @Deprecated
    @CallSuper
    public void onAttach() {
        mCalled = true;
    }

    /**
     * use {@link #onActivityCreated(Bundle)} instead
     */
    @Deprecated
    @CallSuper
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mCalled = true;
    }

    /**
     * use {@link #onDestroyView()} instead
     */
    @Deprecated
    @CallSuper
    public void onDetach() {
        mCalled = true;
    }

    @NonNull
    public abstract View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container,
                                      @Nullable Bundle savedInstanceState);

    @CallSuper
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mCalled = true;
    }

    @CallSuper
    public void onDestroyView() {
        mCalled = true;
    }

    /**
     * use {@link #onDestroyView()} instead
     */
    @Deprecated
    @CallSuper
    public void onDestroy() {
        mCalled = true;
    }

    public final View getView() {
        return this.mView;
    }

    @NonNull
    public final View requireView() {
        View view = getView();
        if (view == null) {
            throw new IllegalStateException("requireView() should not be called before onCreateView() or after onDestroyView()");
        }
        return view;
    }

    @Nullable
    public final Activity getActivity() {
        return this.mActivity;
    }

    @Nullable
    public final Context getApplicationContext() {
        if (this.mActivity == null) {
            return null;
        }
        return this.mActivity.getApplicationContext();
    }

    @Nullable
    public final Context getSceneContext() {
        if (this.mActivity == null) {
            return null;
        }
        if (this.mSceneContext == null) {
            this.mSceneContext = onGetSceneContext();
        }
        return this.mSceneContext;
    }

    @NonNull
    public final Context requireSceneContext() {
        Context sceneContext = getSceneContext();
        if (sceneContext == null) {
            throw new IllegalStateException("Scene " + this + " not attached to an activity.");
        }
        return sceneContext;
    }

    protected Context onGetSceneContext() {
        if (this.mActivity == null) {
            throw new IllegalStateException("onGetContextThemeWrapper() cannot be executed until the "
                    + "Scene is attached to the Activity.");
        }

        if (this.mThemeResource > 0) {
            return new SceneContextThemeWrapper(this.mActivity, this.mThemeResource) {
                @Override
                public Object getSystemService(@NonNull String name) {
                    if (SCENE_SERVICE.equals(name)) {
                        return Scene.this;
                    } else if (Context.LAYOUT_INFLATER_SERVICE.equals(name)) {
                        /*
                         * Some versions of oppo phones will generate a new view when View.onDetachedFromWindow(),
                         * so make a judgment here, find that when the Activity is null, transfer to its super.
                         */
                        if (getActivity() != null) {
                            return getLayoutInflater();
                        }
                    }
                    return super.getSystemService(name);
                }
            };
        } else {
            return new SceneContextThemeWrapper(this.mActivity, this.mActivity.getTheme()) {
                @Override
                public Object getSystemService(@NonNull String name) {
                    if (SCENE_SERVICE.equals(name)) {
                        return Scene.this;
                    } else if (Context.LAYOUT_INFLATER_SERVICE.equals(name)) {
                        /*
                         * Some versions of oppo phones will generate a new view when View.onDetachedFromWindow(),
                         * so make a judgment here, find that when the Activity is null, transfer to its super.
                         */
                        if (getActivity() != null) {
                            return getLayoutInflater();
                        }
                    }
                    return super.getSystemService(name);
                }
            };
        }
    }

    @NonNull
    public final Activity requireActivity() {
        Activity activity = getActivity();
        if (activity == null) {
            throw new IllegalStateException("Scene " + this + " not attached to an activity.");
        }
        return activity;
    }

    @NonNull
    public final Context requireApplicationContext() {
        Context context = getApplicationContext();
        if (context == null) {
            throw new IllegalStateException("Scene " + this + " not attached to a context.");
        }
        return context;
    }

    public final Resources getResources() {
        return requireActivity().getResources();
    }

    public final CharSequence getText(@StringRes int resId) {
        return getResources().getText(resId);
    }

    @NonNull
    public final String getString(@StringRes int resId) {
        return getResources().getString(resId);
    }

    @NonNull
    public final String getString(@StringRes int resId, Object... formatArgs) {
        return getResources().getString(resId, formatArgs);
    }

    @Nullable
    public final Scene getParentScene() {
        return this.mParentScene;
    }

    @NonNull
    public final Scene requireParentScene() {
        Scene parentScene = getParentScene();
        if (parentScene == null) {
            Context context = getApplicationContext();
            if (context == null) {
                throw new IllegalStateException("Scene " + this + " is not attached to any Scene or host");
            } else {
                throw new IllegalStateException("Scene " + this + " is root Scene, not a child Scene");
            }
        }
        return parentScene;
    }

    @Nullable
    public final NavigationScene getNavigationScene() {
        return this.mNavigationScene;
    }

    @NonNull
    public final NavigationScene requireNavigationScene() {
        NavigationScene navigationScene = getNavigationScene();
        if (navigationScene == null) {
            Context context = getApplicationContext();
            if (context == null) {
                throw new IllegalStateException("Scene " + this + " is not attached to any Scene");
            } else {
                throw new IllegalStateException("Scene " + this + " is root Scene");
            }
        }
        return navigationScene;
    }

    @CallSuper
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Activity activity = requireActivity();
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        int visibility = decorView.getSystemUiVisibility();
        if ((visibility & View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) != 0) {
            ViewCompat.requestApplyInsets(decorView);
        } else if ((visibility & View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) != 0) {
            ViewCompat.requestApplyInsets(decorView);
        }

        mCalled = true;
    }

    @CallSuper
    public void onStart() {
        mCalled = true;
    }

    @CallSuper
    public void onViewStateRestored(@NonNull Bundle savedInstanceState) {
        mCalled = true;

        SparseArray<Parcelable> savedStates = savedInstanceState.getSparseParcelableArray(ParcelConstants.KEY_SCENE_VIEWS_TAG);
        if (savedStates != null) {
            mView.restoreHierarchyState(savedStates);
        }

        // restore the focused view
        int focusedViewId = savedInstanceState.getInt(ParcelConstants.KEY_SCENE_FOCUSED_ID_TAG, View.NO_ID);
        if (focusedViewId != View.NO_ID) {
            View needsFocus = mView.findViewById(focusedViewId);
            if (needsFocus != null) {
                needsFocus.requestFocus();
            } else {
                Log.w(TAG, "Previously focused view reported id " + focusedViewId
                        + " during save, but can't be found during restore.");
            }
        }
    }

    @CallSuper
    public void onResume() {
        mCalled = true;
        executePendingActions();
    }

    @CallSuper
    public void onPause() {
        mCalled = true;
    }

    @CallSuper
    public void onSaveInstanceState(@NonNull Bundle outState) {
        mCalled = true;

        if (getArguments() != null) {
            outState.putBoolean(ParcelConstants.KEY_SCENE_HAS_ARGUMENT, true);
            outState.putBundle(ParcelConstants.KEY_SCENE_ARGUMENT, getArguments());
        } else {
            outState.putBoolean(ParcelConstants.KEY_SCENE_HAS_ARGUMENT, false);
        }
        mScope.saveInstance(outState);

        SparseArray<Parcelable> states = new SparseArray<Parcelable>();
        mView.saveHierarchyState(states);
        outState.putSparseParcelableArray(ParcelConstants.KEY_SCENE_VIEWS_TAG, states);

        final View focusedView = mView.findFocus();
        if (focusedView != null && focusedView.getId() != View.NO_ID) {
            outState.putInt(ParcelConstants.KEY_SCENE_FOCUSED_ID_TAG, focusedView.getId());
        }

        dispatchOnSceneSaveInstanceState(this, outState, false);
    }

    @CallSuper
    public void onStop() {
        mCalled = true;
        dispatchVisibleChanged();
    }

    public final <T extends View> T findViewById(@IdRes int id) {
        return getView().findViewById(id);
    }

    public final String getStateHistory() {
        return this.mStateHistoryBuilder.toString();
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public final void executeNowOrScheduleAtNextResume(Runnable runnable) {
        if (getState() == State.RESUMED) {
            runnable.run();
        } else {
            mPendingActionList.add(runnable);
        }
    }

    private FixSceneReuseLifecycleAdapter mLifecycleRegistry = new FixSceneReuseLifecycleAdapter(new LifecycleRegistry(this));

    @NonNull
    @Override
    public final Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }

    @NonNull
    @Override
    public final ViewModelStore getViewModelStore() {
        Scope scope = getScope();
        if (scope.hasServiceInMyScope(ViewModelStoreHolder.class)) {
            return ((ViewModelStoreHolder) scope.getService(ViewModelStoreHolder.class)).get();
        } else {
            ViewModelStore viewModelStore = new ViewModelStore();
            scope.register(ViewModelStoreHolder.class, new ViewModelStoreHolder(viewModelStore));
            return viewModelStore;
        }
    }

    private static class ViewModelStoreHolder implements Scope.Scoped {
        private ViewModelStore mViewModelStore;

        private ViewModelStoreHolder(@NonNull ViewModelStore viewModelStore) {
            this.mViewModelStore = viewModelStore;
        }

        @NonNull
        private ViewModelStore get() {
            return this.mViewModelStore;
        }

        @Override
        public void onUnRegister() {
            this.mViewModelStore.clear();
        }
    }

    public final void setTheme(@StyleRes int resid) {
        if (getView() != null) {
            throw new IllegalStateException("setTheme should be invoked before view is created, the best place is in onCreateView method");
        }
        if (this.mThemeResource != resid) {
            this.mThemeResource = resid;
            if (this.mSceneContext != null) {
                this.mSceneContext.setTheme(this.mThemeResource);
            }
        }
    }

    public final int getTheme() {
        return this.mThemeResource;
    }

    public boolean isVisible() {
        return getState().value >= State.STARTED.value;
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    protected final void dispatchVisibleChanged() {
        boolean visible = isVisible();
        if (visible == mVisibleDispatched) {
            return;
        }
        mVisibleDispatched = visible;
        onVisibleChanged(mVisibleDispatched);
    }

    private void executePendingActions() {
        if (this.mPendingActionList.size() > 0) {
            List<Runnable> copy = new ArrayList<>(this.mPendingActionList);
            for (final Runnable runnable : copy) {
                /*
                 * What if the operation inside the PendingActionList is operating the life cycle?
                 * So we must re-execute executeNowOrScheduleAtNextResume(), and have to wrap to a Runnable
                 */
                executeNowOrScheduleAtNextResume(new Runnable() {
                    @Override
                    public void run() {
                        runnable.run();
                    }
                });
            }
            this.mPendingActionList.removeAll(copy);
        }
    }

    protected void onVisibleChanged(boolean visible) {

    }

    public final Scope getScope() {
        if (this.mScope == null) {
            throw new IllegalStateException("Scope is not created, you can't call before onCreate");
        }
        return this.mScope;
    }

    /**
     * @hide
     * Notify parent of its own life cycle trigger,
     * those response callbacks should only judge children changes rather than their own changes
     */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnSceneCreated(scene, savedInstanceState, scene == this);
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnSceneStarted(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnSceneStarted(scene, scene == this);
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnSceneResumed(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnSceneResumed(scene, scene == this);
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnSceneStopped(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnSceneStopped(scene, scene == this);
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnScenePaused(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnScenePaused(scene, scene == this);
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnSceneSaveInstanceState(scene, outState, scene == this);
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnSceneDestroyed(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnSceneDestroyed(scene, scene == this);
        }
    }

    /**
     * Subclasses can not override equals().
     */
    @Override
    final public boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * Subclasses can not override hashCode().
     */
    @Override
    final public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        Utility.buildShortClassTag(this, sb);
        return sb.toString();
    }
}