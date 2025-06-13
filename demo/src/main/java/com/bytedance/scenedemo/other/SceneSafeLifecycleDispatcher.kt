package com.bytedance.scenedemo.other

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.annotation.IdRes
import com.bytedance.scene.Scene
import com.bytedance.scene.SceneStateSaveStrategy
import com.bytedance.scene.Scope
import com.bytedance.scene.ViewFinder
import com.bytedance.scene.exceptions.OnSaveInstanceStateMethodMissingException
import com.bytedance.scene.navigation.NavigationScene
import com.bytedance.scene.parcel.ParcelConstants

class SceneSafeLifecycleDispatcher(
    @IdRes private val sceneContainerViewId: Int,
    private val viewFinder: ViewFinder?,
    private val activityViewFinder: Activity?,
    private val rootScene: Scene,
    private val rootScopeFactory: Scope.RootScopeFactory,
    private val supportRestore: Boolean,
    private val sceneStateSaveStrategy: SceneStateSaveStrategy? = null
) {
    private enum class State {
        NONE, CREATED, ACTIVITY_CREATED, START, RESUME, PAUSE, STOP
    }

    private var state = State.NONE
    private val handler = Handler(Looper.getMainLooper())
    private var stateSaved = false


    private fun isMainThread(): Boolean {
        return Looper.getMainLooper().thread === Thread.currentThread()
    }

    private var savedState: Bundle? = null

    fun onCreate(activity: Activity, savedInstanceState: Bundle?) {
        val param = savedInstanceState?.getBundle(
            ParcelConstants.KEY_SCENE_ARGUMENT,
        )
        logSceneLifecycleDispatcher {
            "onCreate Start supportRestore: ${supportRestore}, savedInstance: ${savedInstanceState != null}, argument: ${param}, rootScene: ${
                param?.getString(
                    "extra_rootScene"
                )
            }"
        }
        invokeAndThrowExceptionToNextUILoop {
            if (!isMainThread()) {
                throw IllegalStateException("onStarted must run on ui thread")
            }
            check(this.state == State.NONE) { "invoke onDestroyView() first, current state " + this.state.toString() }
            savedState = if (supportRestore) savedInstanceState else null
            this.state = State.CREATED
            this.rootScene.setRootScopeFactory(rootScopeFactory)
            this.rootScene.dispatchAttachActivity(activity)
            this.rootScene.dispatchAttachScene(null)
            if (savedState != null && this.sceneStateSaveStrategy != null) {
                savedState =
                    sceneStateSaveStrategy.onRestoreInstanceState(requireNotNull(savedState))
            }
            if (savedState != null && savedState?.getBoolean(
                    SCENE_LIFECYCLE_MANAGER_ON_SAVE_INSTANCE_STATE_TAG,
                ) == false
            ) {
                throw OnSaveInstanceStateMethodMissingException("savedInstanceState argument is not null, but previous onSaveInstanceState() is missing")
            }
            logSceneLifecycleDispatcher { "dispatchCreate $savedState" }
            this.rootScene.dispatchCreate(savedState)
        }
        logSceneLifecycleDispatcher { "onCreate End ${this.state}" }
    }

    fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        logSceneLifecycleDispatcher { "onActivityCreated Start" }
        invokeAndThrowExceptionToNextUILoop {
            if (!isMainThread()) {
                throw IllegalStateException("onActivityCreated must run on ui thread")
            }
            if (state != State.CREATED) {
                throw IllegalStateException("invoke onDestroyView() first, current state $state")
            }
            if (state == State.ACTIVITY_CREATED) {
                return@invokeAndThrowExceptionToNextUILoop
            }
            stateSaved = false
            state = State.ACTIVITY_CREATED
            val viewGroup: ViewGroup = if (viewFinder != null) {
                this.viewFinder.requireViewById<ViewGroup>(this.sceneContainerViewId)
            } else if (this.activityViewFinder != null) {
                this.activityViewFinder.findViewById<ViewGroup>(this.sceneContainerViewId)
            } else {
                throw IllegalArgumentException("both viewFinder and activityViewFinder are null")
            }
            this.rootScene.dispatchCreateView(savedState, viewGroup)
            if (this.rootScene is NavigationScene && this.rootScene.isViewOwnedByOutside) {
                //skip, reuse activity view, so its parent is not null
                if (this.rootScene.requireView().parent == null) {
                    throw IllegalStateException("You should invoke onActivityCreated after Activity.setContentView")
                }
            } else {
                viewGroup.addView(
                    this.rootScene.requireView(),
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    ),
                )
            }
            this.rootScene.dispatchActivityCreated(savedState)
        }
        logSceneLifecycleDispatcher { "onActivityCreated End ${this.state}" }
    }

    fun onStarted() {
        logSceneLifecycleDispatcher { "onStarted Start" }
        invokeAndThrowExceptionToNextUILoop {
            if (!isMainThread()) {
                throw IllegalStateException("onStarted must run on ui thread")
            }
            if (this.state == State.START) {
                return@invokeAndThrowExceptionToNextUILoop
            }
            if (this.state != State.ACTIVITY_CREATED && this.state != State.STOP) {
                throw IllegalArgumentException("invoke onActivityCreated() or onStop() first, current state " + this.state)
            }
            this.state = State.START
            this.rootScene.dispatchStart()
            stateSaved = false
        }
        logSceneLifecycleDispatcher { "onStarted End ${this.state}" }
    }

    fun onResumed() {
        logSceneLifecycleDispatcher { "onResumed Start" }
        invokeAndThrowExceptionToNextUILoop {
            if (!isMainThread()) {
                throw IllegalStateException("onResumed must run on ui thread")
            }
            if (this.state == State.RESUME) {
                return@invokeAndThrowExceptionToNextUILoop
            }
            if (this.state != State.START && this.state != State.PAUSE) {
                throw IllegalArgumentException("invoke onStart() or onPause() first, current state " + this.state)
            }
            this.state = State.RESUME
            this.rootScene.dispatchResume()
            stateSaved = false
        }
        logSceneLifecycleDispatcher { "onResumed End ${this.state}" }
    }

    fun onPaused() {
        logSceneLifecycleDispatcher { "onPaused Start" }
        invokeAndThrowExceptionToNextUILoop {
            if (!isMainThread()) {
                throw IllegalStateException("onPaused must run on ui thread")
            }
            if (this.state == State.PAUSE) {
                return@invokeAndThrowExceptionToNextUILoop
            }
            if (this.state == State.STOP) {
                onStarted()
                onResumed()
            } else if (this.state == State.START) {
                onResumed()
            }
            if (this.state != State.RESUME) {
                throw IllegalArgumentException("invoke onResume() first, current state " + this.state)
            }
            this.state = State.PAUSE
            this.rootScene.dispatchPause()
        }
        logSceneLifecycleDispatcher { "onPaused End ${this.state}" }
    }

    fun onStopped() {
        logSceneLifecycleDispatcher { "onStopped Start" }
        invokeAndThrowExceptionToNextUILoop {
            if (!isMainThread()) {
                throw IllegalStateException("onStopped must run on ui thread")
            }
            if (this.state == State.STOP) {
                return@invokeAndThrowExceptionToNextUILoop
            }
            if (this.state == State.RESUME) {
                onPaused()
            }
            if (this.state != State.PAUSE && this.state != State.START) {
                throw IllegalArgumentException("invoke onPause() or onStart() first, current state " + this.state)
            }
            this.state = State.STOP
            this.rootScene.dispatchStop()
        }
        logSceneLifecycleDispatcher { "onStopped End ${this.state}" }
    }

    fun onViewDestroyed() {
        logSceneLifecycleDispatcher { "onViewDestroyed Start" }
        invokeAndThrowExceptionToNextUILoop {
            if (!isMainThread()) {
                throw IllegalStateException("onViewDestroyed must run on ui thread")
            }
            if (this.state == State.NONE) {
                return@invokeAndThrowExceptionToNextUILoop
            }
            if (this.state != State.ACTIVITY_CREATED && this.state != State.STOP) {
                throw IllegalArgumentException("invoke onActivityCreated() or onStop() first, current state " + this.state)
            }
            this.state = State.NONE
            this.rootScene.dispatchDestroyView()
            this.rootScene.dispatchDestroy()
            this.rootScene.dispatchDetachScene()
            val activity: Activity = this.rootScene.requireActivity()
            this.rootScene.dispatchDetachActivity()
            this.rootScene.setRootScopeFactory(null)
            sceneStateSaveStrategy?.takeIf { supportRestore }?.let {
                if (!stateSaved) {
                    sceneStateSaveStrategy?.onClear()
                } else if (activity.isFinishing) {
                    sceneStateSaveStrategy?.onClear()
                }
            }

        }
        logSceneLifecycleDispatcher { "onViewDestroyed End ${this.state}" }
    }

    companion object {
        private val SCENE_LIFECYCLE_MANAGER_ON_SAVE_INSTANCE_STATE_TAG =
            "SceneLifecycleManager_onSaveInstanceState_TAG"
    }


    fun onSaveInstanceState(outState: Bundle) {
        logSceneLifecycleDispatcher { "onSaveInstanceState Start" }
        invokeAndThrowExceptionToNextUILoop {
            if (!isMainThread()) {
                throw IllegalStateException("onSaveInstanceState must run on ui thread")
            }
            check(this.state != State.NONE) { "invoke onActivityCreated() first, current state " + this.state.toString() }
            if (this.supportRestore) {
                outState.putString("SCENE", this.rootScene.javaClass.name)
                if (this.sceneStateSaveStrategy != null) {
                    val sceneOutState = Bundle()
                    this.rootScene.dispatchSaveInstanceState(sceneOutState)
                    sceneOutState.putBoolean(
                        SCENE_LIFECYCLE_MANAGER_ON_SAVE_INSTANCE_STATE_TAG,
                        true,
                    )
                    this.sceneStateSaveStrategy.onSaveInstanceState(outState, sceneOutState)
                } else {
                    this.rootScene.dispatchSaveInstanceState(outState)
                    outState.putBoolean(
                        SCENE_LIFECYCLE_MANAGER_ON_SAVE_INSTANCE_STATE_TAG,
                        true,
                    )
                }
                stateSaved = true
            }
        }
        logSceneLifecycleDispatcher { "onSaveInstanceState End ${this.state}" }
    }

    private fun invokeAndThrowExceptionToNextUILoop(action: () -> Unit) {
        try {
            action()
        } catch (throwable: Throwable) {
            handler.post {
                throw throwable
            }
            throw throwable
        }
    }
}

internal fun logScene(msg: () -> String) {
    log("SAFFragmentRootScene", msg)
}

internal fun logSceneLifecycleDispatcher(msg: () -> String) {
    log("SceneSafeLifecycleDispatcher", msg)
}

private fun log(key: String, msg: () -> String) {
    val log = msg.invoke()
    println("${key}: $log")
}