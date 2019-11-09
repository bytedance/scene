package com.bytedance.scene.ktx

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import com.bytedance.scene.Scene
import com.bytedance.scene.SceneComponentFactory
import com.bytedance.scene.SceneLifecycleManager
import com.bytedance.scene.Scope
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor
import com.bytedance.scene.ktx.utility.TestActivity
import com.bytedance.scene.navigation.NavigationScene
import com.bytedance.scene.navigation.NavigationSceneOptions
import org.robolectric.Robolectric

public fun createFromSceneLifecycleManager(rootScene: Scene): NavigationScene {
    val pair = createFromInitSceneLifecycleManager(rootScene)
    val sceneLifecycleManager = pair.first
    sceneLifecycleManager.onStart()
    sceneLifecycleManager.onResume()
    return pair.second
}

public fun createFromInitSceneLifecycleManager(rootScene: Scene): Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> {
    val controller = Robolectric.buildActivity<TestActivity>(TestActivity::class.java).create().start().resume()
    val testActivity = controller.get()
    val navigationScene = NavigationScene()
    val options = NavigationSceneOptions(rootScene.javaClass)
    navigationScene.setArguments(options.toBundle())

    val rootScopeFactory = Scope.RootScopeFactory { Scope.DEFAULT_ROOT_SCOPE_FACTORY.rootScope }

    val sceneComponentFactory = SceneComponentFactory { _, className, _ ->
        if (className == rootScene.javaClass.name) {
            rootScene
        } else null
    }

    navigationScene.defaultNavigationAnimationExecutor = NoAnimationExecutor()
    navigationScene.setRootSceneComponentFactory(sceneComponentFactory)
    val sceneLifecycleManager = SceneLifecycleManager<NavigationScene>()
    sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
            navigationScene, rootScopeFactory, false, null)
    return Pair(sceneLifecycleManager, navigationScene)
}

public fun createFromInitSceneLifecycleManager(activityClass: Class<out Activity>, rootScene: Scene): Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> {
    val controller = Robolectric.buildActivity(activityClass).create().start().resume()
    val testActivity = controller.get()
    val navigationScene = NavigationScene()
    val options = NavigationSceneOptions(rootScene.javaClass)
    navigationScene.setArguments(options.toBundle())

    val rootScopeFactory = Scope.RootScopeFactory { Scope.DEFAULT_ROOT_SCOPE_FACTORY.rootScope }

    val sceneComponentFactory = SceneComponentFactory { _, className, _ ->
        if (className == rootScene.javaClass.name) {
            rootScene
        } else null
    }

    navigationScene.defaultNavigationAnimationExecutor = NoAnimationExecutor()
    navigationScene.setRootSceneComponentFactory(sceneComponentFactory)
    val sceneLifecycleManager = SceneLifecycleManager<NavigationScene>()
    sceneLifecycleManager.onActivityCreated(testActivity, testActivity.findViewById(android.R.id.content),
            navigationScene, rootScopeFactory, false, null)
    return Pair(sceneLifecycleManager, navigationScene)
}
