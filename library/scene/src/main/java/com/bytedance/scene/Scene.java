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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.savedstate.SavedStateRegistry;
import androidx.savedstate.SavedStateRegistryController;
import androidx.savedstate.SavedStateRegistryOwner;

import com.bytedance.scene.parcel.ParcelConstants;
import com.bytedance.scene.utlity.ExceptionsUtility;
import com.bytedance.scene.utlity.FindALSSceneInternalException;
import com.bytedance.scene.utlity.SceneInternalException;
import com.bytedance.scene.utlity.SceneViewTreeLifecycleOwner;
import com.bytedance.scene.utlity.SceneViewTreeSavedStateRegistryOwner;
import com.bytedance.scene.utlity.SceneViewTreeViewModelStoreOwner;
import com.bytedance.scene.utlity.Utility;
import com.bytedance.scene.utlity.ViewRefUtility;
import com.bytedance.scene.view.SceneContextThemeWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 7/30/18.
 * No back stack management
 *
 * When entering:
 *
 * +--------------+  getView() !=null   +---------------+  View attached to view tree   +-------------------+  Lifecycle.Event.ON_CREATE   +---------+  Lifecycle.Event.ON_START   +----------+  Lifecycle.Event.ON_RESUME
 * | onCreateView | ------------------> | onViewCreated | ----------------------------> | onActivityCreated | ---------------------------> | onStart | --------------------------> | onResume | ----------------------------
 * +--------------+                     +---------------+                               +-------------------+                              +---------+                             +----------+
 *
 *
 * When exiting:
 *
 *      Lifecycle.Event.ON_PAUSE   +---------+  Lifecycle.Event.ON_STOP   +--------+  Lifecycle.Event.ON_DESTROY   +---------------+  getView() == null
 *     --------------------------> | onPause | -------------------------> | onStop | ----------------------------> | onDestroyView | --------------------
 *                                 +---------+                            +--------+                               +---------------+
 *
 *
 * Saving and restoring transient UI state：
 *
 * onSaveInstanceState will occur before onDestroyView for applications targeting all platforms.
 *
 * onSaveInstanceState will occur after onStop for applications targeting platforms >= Android9/P
 *
 * saving:
 * +---------------------+     +---------------+
 * | onSaveInstanceState | --> | onDestroyView |
 * +---------------------+     +---------------+
 *
 * restoring:
 * +-------------------+     +---------------------+     +---------+
 * | onActivityCreated | --> | onViewStateRestored | --> | onStart |
 * +-------------------+     +---------------------+     +---------+
 *
 *
 * common usage
 * ```
 * class TestScene : Scene() {
 *     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
 *         return View(requireSceneContext())
 *     }
 *
 *     override fun onActivityCreated(savedInstanceState: Bundle?) {
 *         super.onActivityCreated(savedInstanceState)
 *     }
 *
 *     override fun onStart() {
 *         super.onStart()
 *     }
 *
 *     override fun onResume() {
 *         super.onResume()
 *     }
 *
 *     override fun onPause() {
 *         super.onPause()
 *     }
 *
 *     override fun onStop() {
 *         super.onStop()
 *     }
 *
 *     override fun onDestroyView() {
 *         super.onDestroyView()
 *     }
 * }
 * ```
 */
public abstract class Scene implements LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
    /**
     * use ViewUtility.findSceneByView(View) instead
     */
    @Deprecated
    public static final String SCENE_SERVICE = "scene";
    private static final String TAG = "Scene";

    private Activity mActivity;
    private Context mSceneContext;
    private LayoutInflater mLayoutInflater;
    private View mView;

    private Scene mParentScene;
    private Scope.RootScopeFactory mRootScopeFactory = Scope.DEFAULT_ROOT_SCOPE_FACTORY;
    private Scope mScope;
    private State mState = State.NONE;
    private final StringBuilder mStateHistoryBuilder = new StringBuilder(mState.name);
    private Bundle mArguments;
    private boolean mSceneRestoreEnabled = true;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final List<Runnable> mPendingActionList = new ArrayList<>();
    private boolean mCalled = false;
    private boolean mVisibleDispatched = false;

    @StyleRes
    private int mThemeResource;
    private final FixSceneReuseLifecycleAdapter mLifecycleRegistry = new FixSceneReuseLifecycleAdapter(new LifecycleRegistry(this));
    private SavedStateRegistryController mSavedStateRegistryController;

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

    /**
     * Supply the construction arguments for this Scene.
     * The arguments supplied here will be retained across Scene destroy and
     * creation.
     *
     * @see #getArguments()
     * @see #requireArguments()
     */
    public final void setArguments(@NonNull Bundle arguments) {
        this.mArguments = arguments;
    }

    /**
     * Return the arguments supplied when the Scene was instantiated,
     * if any.
     *
     * @see #setArguments(Bundle)
     * @see #requireArguments()
     */
    @Nullable
    public final Bundle getArguments() {
        return this.mArguments;
    }

    /**
     * Return the arguments supplied when the Scene was instantiated.
     *
     * @throws IllegalStateException if no arguments were supplied to the Scene.
     * @see #setArguments(Bundle)
     * @see #getArguments()
     */
    @NonNull
    public final Bundle requireArguments() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new IllegalStateException("Scene " + this + " does not have any arguments.");
        }
        return arguments;
    }

    public final void disableSceneRestore() {
        this.mSceneRestoreEnabled = false;
    }

    public final boolean isSceneRestoreEnabled() {
        return this.mSceneRestoreEnabled;
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
        mSavedStateRegistryController = SavedStateRegistryController.create(this);
        mSavedStateRegistryController.performRestore(savedInstanceState);

        dispatchOnPreSceneCreated(this, savedInstanceState, false);
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

        return new SceneLayoutInflater(requireActivity(), this);
    }

    /**
     * @hide
     */
    @SuppressLint("WrongConstant")
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
            throw new IllegalArgumentException("onCreateView returned view already has a parent. You must call removeView() on the view's parent first.");
        }

        if (view.getId() == View.NO_ID) {

        }

        //If view's context is not scene context, setTheme() will not work
        Context sceneContext = requireSceneContext();
        Context viewContext = view.getContext();
        if (viewContext != sceneContext && getTheme() != 0) {
            if (viewContext.getSystemService(SCENE_SERVICE) != this) {
                throw new IllegalArgumentException("Scene view's context is incorrect, you should create view with " +
                        "getLayoutInflater() or requireSceneContext() instead");
            }
        }

        SceneViewTreeLifecycleOwner.set(view, this);
        SceneViewTreeViewModelStoreOwner.set(view, this);
        SceneViewTreeSavedStateRegistryOwner.set(view, this);
        view.setTag(R.id.bytedance_scene_view_scene_tag, this);
        view.setSaveFromParentEnabled(false);
        mView = view;
        mCalled = false;
        dispatchOnPreSceneViewCreated(this, savedInstanceState, false);
        onViewCreated(mView, savedInstanceState);
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onViewCreated()");
        }
        dispatchOnSceneViewCreated(this, savedInstanceState, false);
        setState(State.VIEW_CREATED);
    }

    /**
     * We hope the Scene state in {@link com.bytedance.scene.interfaces.ChildSceneLifecycleCallbacks#onSceneActivityCreated(Scene, Bundle)}
     * and {@link #onActivityCreated(Bundle)} are the same, so {@link #dispatchOnSceneActivityCreated(Scene, Bundle, boolean)} is invoked
     * after {@link #onActivityCreated(Bundle)} and before {@link #setState(State)}
     *
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchActivityCreated(@Nullable Bundle savedInstanceState) {
        mCalled = false;
        dispatchOnPreSceneActivityCreated(this, savedInstanceState, false);
        onActivityCreated(savedInstanceState);
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onActivityCreated()");
        }
        dispatchOnSceneActivityCreated(this, savedInstanceState, false);
        if (savedInstanceState != null) {
            dispatchViewStateRestored(savedInstanceState);
        }
        setState(State.ACTIVITY_CREATED);
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchStart() {
        mCalled = false;
        dispatchOnPreSceneStarted(this, false);
        onStart();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onStart()");
        }
        dispatchOnSceneStarted(this, false);
        setState(State.STARTED);
        dispatchVisibleChanged();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
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
        dispatchOnPreSceneResumed(this, false);
        onResume();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onResume()");
        }
        dispatchOnSceneResumed(this, false);
        setState(State.RESUMED);
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchPause() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
        setState(State.STARTED);
        mCalled = false;
        dispatchOnPreScenePaused(this, false);
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
        mSavedStateRegistryController.performSave(outState);
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchStop() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
        setState(State.ACTIVITY_CREATED);
        mCalled = false;
        dispatchOnPreSceneStopped(this, false);
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
        //make sure the View which is in onTouchEvent process will receive MotionEvent.CANCEL before Scene destroyed,
        //otherwise android framework will send it MotionEvent.CANCEL when it is removed from parent ViewGroup,
        //but at that moment, Scene is already be destroyed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ViewRefUtility.cancelViewTouchTargetFromParent(this.mView);
        }

        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        setState(State.NONE);
        mCalled = false;
        dispatchOnPreSceneViewDestroyed(this, false);
        onDestroyView();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onDestroyView()");
        }
        dispatchOnSceneViewDestroyed(this, false);
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
        dispatchOnPreSceneDestroyed(this, false);
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
            if (this.viewModelStore != null) {
                this.viewModelStore.clear();
                this.viewModelStore = null;
            }
        }
        this.mScope = null;
        // Must be called last, in case someone do add in onDestroy/onDetach
        mPendingActionList.clear();
    }

    /**
     * use {@link #onActivityCreated(Bundle)} instead
     */
    @Deprecated
    @MainThread
    @CallSuper
    public void onAttach() {
        mCalled = true;
    }

    /**
     * use {@link #onActivityCreated(Bundle)} instead
     */
    @Deprecated
    @MainThread
    @CallSuper
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mCalled = true;
    }

    /**
     * Called when the Scene is no longer attached to its activity.  This
     * is called after {@link #onDestroy()}.
     * <p>
     * use {@link #onDestroyView()} instead
     */
    @Deprecated
    @MainThread
    @CallSuper
    public void onDetach() {
        mCalled = true;
    }

    /**
     * Called to have the Scene instantiate its user interface view.
     * <p>It is recommended to <strong>only</strong> inflate the layout in this method and move
     * logic that operates on the returned View to {@link #onViewCreated(View, Bundle)}.or
     * {@link #onActivityCreated(Bundle)}
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the Scene.
     * @param container          This is the parent view that the Scene's UI should be attached to.
     * @param savedInstanceState If non-null, this Scene is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the Scene's UI.
     * @see #onDestroyView
     */
    @MainThread
    @NonNull
    public abstract View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container,
                                      @Nullable Bundle savedInstanceState);

    @MainThread
    @CallSuper
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mCalled = true;
    }

    /**
     * Called when the Scene is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDestroy()}.
     * Then, {@link #mView} is reset to null.
     */
    @CallSuper
    @MainThread
    public void onDestroyView() {
        mCalled = true;
    }

    /**
     * This is called after {@link #onDestroyView()} ()} and before {@link #onDetach()}.
     * <p>
     * use {@link #onDestroyView()} instead
     */
    @Deprecated
    @MainThread
    @CallSuper
    public void onDestroy() {
        mCalled = true;
    }

    /**
     * Get the root view for the Scene's layout (the one returned by {@link #onCreateView}),
     * if provided.
     *
     * @return The Scene's root view, or null if Scene view is not created or has been destroyed.
     */
    public final View getView() {
        return this.mView;
    }

    /**
     * Get the root view for the Scene's layout (the one returned by {@link #onCreateView}).
     *
     * @throws IllegalStateException if current Scene view is not created or has been destroyed.
     * @see #getView()
     */
    @NonNull
    public final View requireView() {
        View view = getView();
        if (view == null) {
            throw new IllegalStateException("requireView() should not be called before onCreateView() or after onDestroyView()");
        }
        return view;
    }

    /**
     * Return the {@link Activity} this Scene is currently associated with.
     *
     * @see #requireActivity()
     */
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

    /**
     * Return the {@link Activity} this Scene is currently associated with.
     *
     * @throws IllegalStateException if not currently associated with an activity
     * @see #getActivity()
     */
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

    @NonNull
    public final Resources getResources() {
        return requireActivity().getResources();
    }

    /**
     * Return a localized, styled CharSequence from the application's package's
     * default string table.
     *
     * @param resId Resource id for the CharSequence text
     */
    @NonNull
    public final CharSequence getText(@StringRes int resId) {
        return getResources().getText(resId);
    }

    /**
     * Return a localized string from the application's package's
     * default string table.
     *
     * @param resId Resource id for the string
     */
    @NonNull
    public final String getString(@StringRes int resId) {
        return getResources().getString(resId);
    }

    /**
     * Return a localized formatted string from the application's package's
     * default string table, substituting the format arguments as defined in
     * {@link java.util.Formatter} and {@link java.lang.String#format}.
     *
     * @param resId      Resource id for the format string
     * @param formatArgs The format arguments that will be used for substitution.
     */
    @NonNull
    public final String getString(@StringRes int resId, Object... formatArgs) {
        return getResources().getString(resId, formatArgs);
    }

    /**
     * Returns the parent NavigationScene or GroupScene containing this Scene.  If this Scene
     * is attached directly to an Activity, returns null.
     */
    @Nullable
    public final Scene getParentScene() {
        return this.mParentScene;
    }

    /**
     * Returns the parent NavigationScene or GroupScene containing this Scene.
     *
     * @throws IllegalStateException if this Scene is attached directly to an Activity or
     *                               not attached.
     * @see #getParentScene()
     */
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

    @MainThread
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

    /**
     * Called when the Scene is visible to the user. It will be followed by {@link #onResume} if it
     * is not overlapped with other translucent Scene.
     *
     * @see #onActivityCreated
     * @see #onStop
     * @see #onResume
     */
    @MainThread
    @CallSuper
    public void onStart() {
        mCalled = true;
    }

    @MainThread
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

    /**
     * Called when the Scene is visible to the user and it is not overlapped with other translucent Scene.
     *
     * @see #onStart()
     * @see #onPause
     */
    @MainThread
    @CallSuper
    public void onResume() {
        mCalled = true;
        executePendingActions();
    }

    @MainThread
    @CallSuper
    public void onPause() {
        mCalled = true;
    }

    @MainThread
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

    @MainThread
    @CallSuper
    public void onStop() {
        mCalled = true;
        dispatchVisibleChanged();
    }

    /**
     * Finds the first descendant view with the given ID, or {@code null} if there is no
     * matching view in the hierarchy.
     *
     * @param id the ID to search for
     * @return a view with given ID if found, or {@code null} otherwise
     * @see #requireViewById(int)
     */
    @Nullable
    public final <T extends View> T findViewById(@IdRes int id) {
        View view = getView();
        if (view == null) {
            return null;
        }
        return view.findViewById(id);
    }

    /**
     * Finds the first descendant view with the given ID, or throws an IllegalArgumentException
     * if there is no matching view in the hierarchy.
     *
     * @param id the ID to search for
     * @return a view with given ID
     * @throws IllegalArgumentException if no matching view found
     * @see #findViewById(int)
     */
    @NonNull
    public final <T extends View> T requireViewById(@IdRes int id) {
        T view = requireView().findViewById(id);
        if (view == null) {
            try {
                String viewIdName = getResources().getResourceName(id);
                throw new IllegalArgumentException(" " + viewIdName + " view not found");
            } catch (Resources.NotFoundException exception) {
                throw new IllegalArgumentException(" " + id + " view not found");
            }
        }
        return view;
    }

    @NonNull
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

    /**
     * Get a {@link Lifecycle} that represents this Scene's lifecycle, Scene has just one lifecycle,
     * the lifecycle of Scene and the lifecycle of Scene'view are the same.
     * <p>
     * Namely, the lifecycle of the Scene is:
     * <ol>
     * <li>{@link Lifecycle.Event#ON_CREATE created} after {@link #onActivityCreated(Bundle)}</li>
     * <li>{@link Lifecycle.Event#ON_START started} after {@link #onStart()}</li>
     * <li>{@link Lifecycle.Event#ON_RESUME resumed} after {@link #onResume()}</li>
     * <li>{@link Lifecycle.Event#ON_PAUSE paused} before {@link #onPause()}</li>
     * <li>{@link Lifecycle.Event#ON_STOP stopped} before {@link #onStop()}</li>
     * <li>{@link Lifecycle.Event#ON_DESTROY destroyed} before {@link #onDestroyView()}</li>
     * </ol>
     */
    @MainThread
    @NonNull
    @Override
    public final Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }

    private ViewModelStore viewModelStore;

    /**
     * Returns the {@link ViewModelStore} associated with this Scene
     */
    @MainThread
    @NonNull
    @Override
    public final ViewModelStore getViewModelStore() {
        if (SceneGlobalConfig.createSceneViewModelStoreBySceneSelf) {
            synchronized (this) {
                if (viewModelStore == null) {
                    viewModelStore = new ViewModelStore();
                }
                return viewModelStore;
            }
        }

        synchronized (this) {
            Scope scope = getScope();
            ViewModelStoreHolder viewModelStoreHolder = scope.getServiceInMyScope(ViewModelStoreHolder.class);
            if (viewModelStoreHolder != null) {
                final Class<? extends Scene> previousSceneClass = viewModelStoreHolder.mSceneClass;
                if (previousSceneClass != null && previousSceneClass != this.getClass()) {
                    ExceptionsUtility.invokeAndThrowExceptionToNextUILoop(new Runnable() {
                        @Override
                        public void run() {
                            throw new SceneInternalException("Scene getViewModelStore() error, ViewModelStoreHolder type mismatch, request " + this.getClass().getName() + " but get " + previousSceneClass.getName());
                        }
                    });
                }
            } else {
                ViewModelStore viewModelStore = new ViewModelStore();
                viewModelStoreHolder = new ViewModelStoreHolder(viewModelStore, this.getClass());
                scope.registerInMyScope(ViewModelStoreHolder.class, viewModelStoreHolder);
            }
            return viewModelStoreHolder.get();
        }
    }

    private static class ViewModelStoreHolder implements Scope.Scoped {
        private ViewModelStore mViewModelStore;
        private Class<? extends Scene> mSceneClass;

        private ViewModelStoreHolder(@NonNull ViewModelStore viewModelStore,
                                     @Nullable Class<? extends Scene> sceneClass) {
            this.mViewModelStore = viewModelStore;
            this.mSceneClass = sceneClass;
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

    @NonNull
    @Override
    public final SavedStateRegistry getSavedStateRegistry() {
        if (mSavedStateRegistryController == null) {
            throw new IllegalStateException("SavedStateRegistry is not created, you can't call before onCreate");
        }
        return mSavedStateRegistryController.getSavedStateRegistry();
    }

    /**
     * Use another theme instead of inherit from Activity theme
     * <p>
     * This method will modify onCreateView inflated view context, View.getContext() will return ContextWrapper instead
     * of Activity
     */
    public final void setTheme(@StyleRes int resId) {
        if (getView() != null) {
            throw new IllegalStateException("setTheme should be invoked before view is created, the best place is in onCreateView method");
        }
        if (this.mThemeResource != resId) {
            this.mThemeResource = resId;
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
                runnable.run();
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

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnPreSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnPreSceneCreated(scene, savedInstanceState, scene == this);
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnPreSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnPreSceneViewCreated(scene, savedInstanceState, scene == this);
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnPreSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnPreSceneActivityCreated(scene, savedInstanceState, scene == this);
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnPreSceneStarted(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnPreSceneStarted(scene, scene == this);
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnPreSceneResumed(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnPreSceneResumed(scene, scene == this);
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnPreScenePaused(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnPreScenePaused(scene, scene == this);
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnPreSceneStopped(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnPreSceneStopped(scene, scene == this);
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnPreSceneViewDestroyed(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnPreSceneViewDestroyed(scene, scene == this);
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnPreSceneDestroyed(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnPreSceneDestroyed(scene, scene == this);
        }
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
    public void dispatchOnSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnSceneViewCreated(scene, savedInstanceState, scene == this);
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    public void dispatchOnSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnSceneActivityCreated(scene, savedInstanceState, scene == this);
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
    public void dispatchOnSceneViewDestroyed(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnSceneViewDestroyed(scene, scene == this);
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

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        Utility.buildShortClassTag(this, sb);
        return sb.toString();
    }

    @NonNull
    public final String getDebugSceneHierarchy() {
        return Utility.getViewHierarchy(this);
    }
}