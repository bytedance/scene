package com.bytedance.scene.navigation.post;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.SceneGlobalConfig;
import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.Scope;
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;

import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

public class NavigationSourceSupportPostUtility {
    public static NavigationScene createFromSceneLifecycleManager(final Scene rootScene) {
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = createFromInitSceneLifecycleManager(rootScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        return pair.second;
    }

    public static Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> createFromInitSceneLifecycleManager(final Scene rootScene) {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(rootScene.getClass());
        options.setOnlyRestoreVisibleScene(true);
        options.setUsePostInLifecycle(true);
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
