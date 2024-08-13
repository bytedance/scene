package com.bytedance.scene.navigation;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import com.bytedance.scene.*;
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

public class NavigationSourceUtility {
    public static SceneDelegate createFromNavigationSceneUtility(final Scene rootScene) {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        return NavigationSceneUtility.setupWithActivity(testActivity, null, rootScene.getClass(), new SceneComponentFactory() {
            @Override
            public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                if (className.equals(rootScene.getClass().getName())) {
                    return rootScene;
                }
                return null;
            }
        }, false);
    }

    public static NavigationScene createFromSceneLifecycleManager(final Scene rootScene) {
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = createFromInitSceneLifecycleManager(rootScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        return pair.second;
    }

    public static Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> createFromInitSceneLifecycleManager(final Scene rootScene) {
        return createFromInitSceneLifecycleManager(rootScene, false);
    }

    public static Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> createFromInitSceneLifecycleManager(final Scene rootScene, boolean separateCreate) {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(rootScene.getClass());
        SceneGlobalConfig.useActivityCompatibleLifecycleStrategy = true;
        options.setOnlyRestoreVisibleScene(true);
        options.setUsePostInLifecycle(true);
        navigationScene.setSeparateCreateFromCreateView(separateCreate);
        navigationScene.setArguments(options.toBundle());

        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };

        SceneComponentFactory sceneComponentFactory = new SceneComponentFactory() {
            @Override
            public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                if (className.equals(rootScene.getClass().getName())) {
                    return rootScene;
                }
                return null;
            }
        };

        navigationScene.setDefaultNavigationAnimationExecutor(new NoAnimationExecutor());
        navigationScene.setRootSceneComponentFactory(sceneComponentFactory);

        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, rootScopeFactory,
                false, null);
        return Pair.create(sceneLifecycleManager, navigationScene);
    }

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
