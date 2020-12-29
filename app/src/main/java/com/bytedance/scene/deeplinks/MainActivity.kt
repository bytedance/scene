package com.bytedance.scene.deeplinks

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bytedance.scene.NavigationSceneUtility
import com.bytedance.scene.Scene
import com.bytedance.scene.SceneDelegate
import com.bytedance.scene.group.GroupScene
import com.bytedance.scenerouter.annotation.SceneUrl
import com.bytedance.scenerouter.core.SceneRouters

//TODO support url query argument
class MainActivity : AppCompatActivity() {
    private lateinit var mDelegate: SceneDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
        this.mDelegate = NavigationSceneUtility.setupWithActivity(this, getHomeSceneClass())
            .supportRestore(false).build()

        val action: String? = intent?.action
        val data: Uri? = intent?.data
        handleDeepLink(action, data)
    }

    private fun getHomeSceneClass(): Class<out Scene> {
        return MainScene::class.java
    }

    override fun onBackPressed() {
        if (!this.mDelegate.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val action: String? = intent?.action
        val data: Uri? = intent?.data
        handleDeepLink(action, data)
    }

    private fun handleDeepLink(action: String?, data: Uri?) {
        if (data == null || data.path.isNullOrBlank()) {
            return
        }
        val navigationScene = this.mDelegate.navigationScene ?: return
        val uri = data.path!!.removePrefix("/demo")

        if (navigationScene.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            SceneRouters.of(navigationScene).url(uri).argument("uri", uri).open()
        } else {
            navigationScene.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_START)
                fun onCreate() {
                    SceneRouters.of(navigationScene).url(uri).argument("uri", uri).open()
                }
            })
        }
    }
}

class MainScene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return FrameLayout(requireSceneContext())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}

//TODO url match
@SceneUrl("/test1")
class AScene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return FrameLayout(requireSceneContext()).apply {
            setBackgroundColor(Color.RED)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}

@SceneUrl("/test2")
class BScene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return FrameLayout(requireSceneContext()).apply {
            setBackgroundColor(Color.YELLOW)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}