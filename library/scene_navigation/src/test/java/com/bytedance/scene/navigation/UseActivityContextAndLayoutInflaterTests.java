package com.bytedance.scene.navigation;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.Scope;
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class UseActivityContextAndLayoutInflaterTests {
    @Test
    public void testUseActivityContextAndLayoutInflaterFalse() {
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = createFromInitSceneLifecycleManager(false);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        NavigationScene navigationScene = pair.second;
        assertNotSame(navigationScene.requireSceneContext(), navigationScene.requireActivity());
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onDestroyView();
    }

    @Test
    public void testUseActivityContextAndLayoutInflaterTrue() {
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = createFromInitSceneLifecycleManager(true);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        NavigationScene navigationScene = pair.second;
        assertSame(navigationScene.requireSceneContext(), navigationScene.requireActivity());
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onDestroyView();
    }

    public static Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> createFromInitSceneLifecycleManager(boolean useActivityContextAndLayoutInflater) {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(TestScene.class);
        options.setOnlyRestoreVisibleScene(true);
        options.setUsePostInLifecycle(true);
        options.setDrawWindowBackground(false);
        options.setUseActivityContextAndLayoutInflater(useActivityContextAndLayoutInflater);
        navigationScene.setArguments(options.toBundle());

        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };

        navigationScene.setDefaultNavigationAnimationExecutor(new NoAnimationExecutor());

        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout, navigationScene, rootScopeFactory, false, null);
        return Pair.create(sceneLifecycleManager, navigationScene);
    }

    public static class TestScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
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

