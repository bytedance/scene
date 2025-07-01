package com.bytedance.scenedemo.other

import android.content.res.Configuration
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.bytedance.scene.Scope
import com.bytedance.scene.navigation.NavigationScene
import com.bytedance.scene.navigation.NavigationSceneOptions
import com.bytedance.scenedemo.MainScene
import com.bytedance.scenedemo.R

/**
 * Created by jiangqi on 2024/10/30
 * @author jiangqi@bytedance.com
 */
class SAFActivity : AppCompatActivity() {

    private var sceneLifecycleDispatcher: SceneSafeLifecycleDispatcher? = null
    private var navigationScene: NavigationScene? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScene()
        dispatchSceneCreated(savedInstanceState)
        val frameLayout = FrameLayout(this).apply {
            id = R.id.saa_container
        }
        setContentView(frameLayout)
        navigationScene?.setOutsideView(frameLayout)
        dispatchSceneActivityCreated(savedInstanceState)
    }

    override fun onBackPressed() {
        if (navigationScene?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        sceneLifecycleDispatcher?.onStarted()
    }

    override fun onResume() {
        super.onResume()
        sceneLifecycleDispatcher?.onResumed()
    }

    override fun onPause() {
        super.onPause()
        sceneLifecycleDispatcher?.onPaused()
    }

    override fun onStop() {
        super.onStop()
        sceneLifecycleDispatcher?.onStopped()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        sceneLifecycleDispatcher?.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        sceneLifecycleDispatcher?.onViewDestroyed()
    }

    private fun initSceneEnvironment(): Pair<NavigationScene?, SceneSafeLifecycleDispatcher> {
        val rootScene = MainScene()
        val navigationSceneOptions = NavigationSceneOptions(rootScene::class.java, Bundle())
        navigationSceneOptions.setDrawWindowBackground(false)
        navigationSceneOptions.setFixSceneWindowBackgroundEnabled(false)
        navigationSceneOptions.setOnlyRestoreVisibleScene(true)
        navigationSceneOptions.setSceneBackground(0)
        navigationSceneOptions.useActivityContextAndLayoutInflater = true
        navigationSceneOptions.lazyLoadNavigationSceneUnnecessaryView = true
        navigationSceneOptions.reduceColdStartCallStack = true
        navigationSceneOptions.optimizedViewLayer = true
        navigationSceneOptions.useWindowFocusChangedDispatch = true
        navigationSceneOptions.reuseOutsideView = true
        navigationSceneOptions.onlyDispatchToTopSceneWindowInsets = true
        navigationSceneOptions.useExtraViewToBlockGesture = true

        val navigationSceneArguments = navigationSceneOptions.toBundle()
        navigationSceneArguments.classLoader = this.classLoader
        val scene = NavigationScene()
        scene.setArguments(navigationSceneArguments)

        if (scene.arguments?.getString("extra_rootScene").isNullOrEmpty()) {
            throw IllegalArgumentException("Create NavigationScene but there is no RootSceneClass in argument")
        }

        scene.setRootSceneComponentFactory { cl, className, bundle ->
            rootScene
        }

        val dispatcher = SceneSafeLifecycleDispatcher(
            R.id.saa_container,
            null,
            this,
            scene,
            { Scope(null, null, "saf_root_scene") },
            true,
            null
        )
        return scene to dispatcher
    }

    private fun initScene() {
        val (navigationScene, second) = initSceneEnvironment()
        navigationScene?.isSeparateCreateFromCreateView = true
        navigationScene?.setInitRootSceneOnCreate(true)
        navigationScene?.addNavigationListener(
            this,
        ) { _, to, _ ->

        }
        sceneLifecycleDispatcher = second
        this.navigationScene = navigationScene
    }

    protected fun dispatchSceneCreated(savedInstanceState: Bundle?) {
        sceneLifecycleDispatcher?.onCreate(this, savedInstanceState)
    }

    protected fun dispatchSceneActivityCreated(savedInstanceState: Bundle?) {
        sceneLifecycleDispatcher?.onActivityCreated(this, savedInstanceState)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        navigationScene?.onConfigurationChanged(newConfig)
    }
}