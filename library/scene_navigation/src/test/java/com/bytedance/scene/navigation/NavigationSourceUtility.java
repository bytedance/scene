package com.bytedance.scene.navigation;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import com.bytedance.scene.*;
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;
import com.bytedance.scene.navigation.reuse.IReusePool;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

public class NavigationSourceUtility {
    /**
     * Creates a SceneDelegate using NavigationSceneUtility
     */
    public static SceneDelegate createFromNavigationSceneUtility(final Scene rootScene) {
        TestActivity testActivity = createTestActivity();
        return NavigationSceneUtility.setupWithActivity(
                testActivity,
                null,
                rootScene.getClass(),
                createSceneComponentFactory(rootScene),
                false,
                false);
    }

    /**
     * Creates NavigationScene with complete lifecycle initialization
     */
    public static NavigationScene createFromSceneLifecycleManager(final Scene rootScene) {
        return createFromSceneLifecycleManager(rootScene, null, false);
    }

    /**
     * Creates NavigationScene with complete lifecycle initialization (with ReusePool)
     */
    public static NavigationScene createFromSceneLifecycleManager(final Scene rootScene, IReusePool reusePool) {
        return createFromSceneLifecycleManager(rootScene, reusePool, false);
    }

    /**
     * Creates NavigationScene with complete lifecycle initialization (with custom parameters)
     */
    public static NavigationScene createFromSceneLifecycleManager(final Scene rootScene, IReusePool reusePool, boolean separateCreate) {
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair =
                createFromInitSceneLifecycleManager(rootScene, reusePool, separateCreate);
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = pair.first;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        return pair.second;
    }

    /**
     * Creates NavigationScene in initial state (without completing lifecycle)
     */
    public static Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> createFromInitSceneLifecycleManager(final Scene rootScene) {
        return createFromInitSceneLifecycleManager(rootScene, null, false);
    }

    /**
     * Creates NavigationScene in initial state (with ReusePool)
     */
    public static Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> createFromInitSceneLifecycleManager(final Scene rootScene, IReusePool reusePool) {
        return createFromInitSceneLifecycleManager(rootScene, reusePool, false);
    }

    /**
     * Creates NavigationScene in initial state (with separateCreate parameter)
     */
    public static Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> createFromInitSceneLifecycleManager(final Scene rootScene, boolean separateCreate) {
        return createFromInitSceneLifecycleManager(rootScene, null, separateCreate);
    }

    /**
     * Creates NavigationScene in initial state (core implementation method)
     */
    public static Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> createFromInitSceneLifecycleManager(
            final Scene rootScene,
            IReusePool reusePool,
            boolean separateCreate) {

        TestActivity testActivity = createTestActivity();
        NavigationScene navigationScene = createNavigationScene(rootScene, reusePool, separateCreate);

        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        sceneLifecycleManager.onActivityCreated(
                testActivity,
                testActivity.mFrameLayout,
                navigationScene,
                createRootScopeFactory(),
                false,
                null);

        return Pair.create(sceneLifecycleManager, navigationScene);
    }

    /**
     * Creates test Activity
     */
    private static TestActivity createTestActivity() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class)
                .create()
                .start()
                .resume();
        return controller.get();
    }

    /**
     * Creates NavigationScene instance
     */
    private static NavigationScene createNavigationScene(Scene rootScene, IReusePool reusePool, boolean separateCreate) {
        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(rootScene.getClass());

        SceneGlobalConfig.useActivityCompatibleLifecycleStrategy = true;
        options.setOnlyRestoreVisibleScene(true);

        navigationScene.setSeparateCreateFromCreateView(separateCreate);
        navigationScene.setInitRootSceneOnCreate(separateCreate);
        navigationScene.setArguments(options.toBundle());

        if (reusePool != null) {
            navigationScene.setReusePool(reusePool);
        }

        navigationScene.setDefaultNavigationAnimationExecutor(new NoAnimationExecutor());
        navigationScene.setRootSceneComponentFactory(createSceneComponentFactory(rootScene));

        return navigationScene;
    }

    /**
     * Creates RootScopeFactory
     */
    private static Scope.RootScopeFactory createRootScopeFactory() {
        return new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };
    }

    /**
     * Creates SceneComponentFactory
     */
    private static SceneComponentFactory createSceneComponentFactory(final Scene rootScene) {
        return (cl, className, bundle) -> {
            if (className.equals(rootScene.getClass().getName())) {
                return rootScene;
            }
            return null;
        };
    }

    /**
     * Test Activity class
     */
    public static class TestActivity extends Activity {
        public FrameLayout mFrameLayout;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mFrameLayout = new FrameLayout(this);
            setContentView(mFrameLayout);
        }
    }
}