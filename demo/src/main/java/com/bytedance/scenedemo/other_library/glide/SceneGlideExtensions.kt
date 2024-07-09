package com.bytedance.scenedemo.other_library.glide

import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.manager.LifecycleListener
import com.bumptech.glide.manager.RequestManagerTreeNode
import com.bumptech.glide.util.Util
import com.bytedance.scene.Scene
import com.bytedance.scene.SceneViewModelProviders
import com.bytedance.scene.utlity.ThreadUtility
import java.util.*

@MainThread
fun Scene.requireGlide(): RequestManager {
    ThreadUtility.checkUIThread()
    return createRequestManagerIfNeeded(this)
}

internal class GlideViewModel : ViewModel() {
    var requestManager: RequestManager? = null
}

private fun createRequestManagerIfNeeded(scene: Scene): RequestManager {
    if (scene.lifecycle.currentState == Lifecycle.State.DESTROYED) {
        throw IllegalStateException("Scene is already destroyed.")
    }

    val vm = SceneViewModelProviders.of(scene).get(GlideViewModel::class.java)
    if (vm.requestManager == null) {
        val appContext = scene.requireApplicationContext()
        vm.requestManager = RequestManager(
                Glide.get(appContext),
                SceneGlideLifecycle(scene.lifecycle),
                SceneRequestManagerTreeNode(), appContext
        )
    }
    return vm.requestManager!!
}

private class SceneRequestManagerTreeNode : RequestManagerTreeNode {
    override fun getDescendants(): Set<RequestManager> {
        return emptySet()
    }
}

private class SceneGlideLifecycle(private val lifecycle: Lifecycle) : com.bumptech.glide.manager.Lifecycle {
    private val glideLifecycleListeners = Collections.newSetFromMap(WeakHashMap<LifecycleListener, Boolean>())
    private val lifecycleObserver = LifecycleAdapter()

    init {
        lifecycle.addObserver(lifecycleObserver)
    }

    inner class LifecycleAdapter : androidx.lifecycle.LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart(owner: LifecycleOwner) {
            val listeners = Util.getSnapshot(glideLifecycleListeners)
            for (listener in listeners) {
                listener.onStart()
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop(owner: LifecycleOwner) {
            val listeners = Util.getSnapshot(glideLifecycleListeners)
            for (listener in listeners) {
                listener.onStop()
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy(owner: LifecycleOwner) {
            val listeners = Util.getSnapshot(glideLifecycleListeners)
            for (listener in listeners) {
                listener.onDestroy()
            }
            glideLifecycleListeners.clear()
            lifecycle.removeObserver(this)
        }
    }

    override fun addListener(listener: LifecycleListener) {
        glideLifecycleListeners.add(listener)
        when (lifecycle.currentState) {
            Lifecycle.State.STARTED -> listener.onStart()
            Lifecycle.State.DESTROYED -> listener.onDestroy()
            else -> listener.onStop()
        }
    }

    override fun removeListener(listener: LifecycleListener) {
        glideLifecycleListeners.remove(listener)
    }
}