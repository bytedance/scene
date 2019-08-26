package com.bytedance.scene;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.widget.FrameLayout;
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
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
        Pair<SceneLifecycleManager, NavigationScene> pair = createFromInitSceneLifecycleManager(rootScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        return pair.second;
    }

    public static Pair<SceneLifecycleManager, NavigationScene> createFromInitSceneLifecycleManager(final Scene rootScene) {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(rootScene.getClass());
        navigationScene.setArguments(options.toBundle());

        NavigationScene.NavigationSceneHost navigationSceneHost = new NavigationScene.NavigationSceneHost() {
            @Override
            public boolean isSupportRestore() {
                return false;
            }

            @Override
            public void startActivityForResult(@NonNull Intent intent, int requestCode) {

            }

            @Override
            public void requestPermissions(@NonNull String[] permissions, int requestCode) {

            }
        };

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

        SceneLifecycleManager sceneLifecycleManager = new SceneLifecycleManager();
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, navigationSceneHost, rootScopeFactory,
                sceneComponentFactory, null);
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
