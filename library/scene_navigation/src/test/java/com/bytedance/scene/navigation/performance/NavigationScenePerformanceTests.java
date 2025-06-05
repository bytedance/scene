package com.bytedance.scene.navigation.performance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.SceneGlobalConfig;
import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.Scope;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scene.navigation.NavigationSourceUtility;
import com.bytedance.scene.utlity.ViewIdGenerator;
import com.bytedance.scene.view.AnimationContainerLayout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

/**
 * test setLazyLoadNavigationSceneUnnecessaryView, NavigationScene must have DefaultNavigationAnimationExecutor
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NavigationScenePerformanceTests {
    @Test
    public void test() {
        final TestScene groupScene = new TestScene();
        TestChildScene secondScene = new TestChildScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationScenePerformanceTests.createFromInitSceneLifecycleManager(groupScene);
        assertNotNull(groupScene.getView().getParent());
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        NavigationScene navigationScene = pair.second;

        assertEquals(1, ((ViewGroup) navigationScene.getView()).getChildCount());

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.RESUMED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

        if (!navigationScene.getNavigationSceneOptions().getOptimizedViewLayer()) {
            assertEquals(2, ((ViewGroup) navigationScene.getView()).getChildCount());
            assertEquals(AnimationContainerLayout.class, ((ViewGroup) navigationScene.getView()).getChildAt(1).getClass());
        }

        View groupSceneView = groupScene.getView();
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onDestroyView();
        assertEquals(secondScene.getState(), State.NONE);
        assertEquals(groupScene.getState(), State.NONE);
        assertNotNull(groupSceneView.getParent());
    }

    public static class TestScene extends GroupScene {
        public final int mId;

        public TestScene() {
            mId = ViewIdGenerator.generateViewId();
        }

        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new FrameLayout(requireSceneContext());
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            view.setId(mId);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            assertTrue(View.VISIBLE == getView().getVisibility() || View.GONE == getView().getVisibility());
        }

        @Override
        public void onStart() {
            super.onStart();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onResume() {
            super.onResume();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onPause() {
            super.onPause();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onStop() {
            super.onStop();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            assertTrue(View.VISIBLE == getView().getVisibility() || View.GONE == getView().getVisibility());
        }
    }

    public static class TestChildScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            assertTrue(View.VISIBLE == getView().getVisibility() || View.GONE == getView().getVisibility());
        }

        @Override
        public void onStart() {
            super.onStart();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onResume() {
            super.onResume();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onPause() {
            super.onPause();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onStop() {
            super.onStop();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            assertTrue(View.VISIBLE == getView().getVisibility() || View.GONE == getView().getVisibility());
        }
    }

    public static Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> createFromInitSceneLifecycleManager(final Scene rootScene) {
        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();
        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(rootScene.getClass());
        SceneGlobalConfig.useActivityCompatibleLifecycleStrategy = true;
        options.setOnlyRestoreVisibleScene(true);
        options.setUsePostInLifecycle(true);
        options.setLazyLoadNavigationSceneUnnecessaryView(true);
        navigationScene.setSeparateCreateFromCreateView(true);
        navigationScene.setInitRootSceneOnCreate(true);
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
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout, navigationScene, rootScopeFactory, false, null);
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

