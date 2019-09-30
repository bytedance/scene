package com.bytedance.scene;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NavigationSceneUtilityTests {
    @Test
    public void test() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).build();
        controller.start().resume();
        assertNotNull(sceneDelegate.getNavigationScene());
        controller.pause().stop().destroy();
    }

    @Test
    public void testSetRootScopeFactory() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        final TestScene scene = new TestScene();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class)
                .rootSceneComponentFactory(new SceneComponentFactory() {
                    @Override
                    public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                        return scene;
                    }
                }).build();
        controller.start().resume();
        assertEquals(scene, sceneDelegate.getNavigationScene().getCurrentScene());
    }

    @Test
    public void testAbandon() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).build();
        controller.start().resume();
        sceneDelegate.abandon();
        sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).build();
        sceneDelegate.abandon();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTagDuplicateException() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        NavigationSceneUtility.setupWithActivity(activity, TestScene.class).build();
        controller.start().resume();
        NavigationSceneUtility.setupWithActivity(activity, TestScene.class).build();
    }

    @Test
    public void testDrawWindowBackground() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().resume();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).fixSceneWindowBackgroundEnabled(true).build();
        assertNotNull(sceneDelegate.getNavigationScene().requireView().getBackground());
        sceneDelegate.abandon();

        sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).drawWindowBackground(false).build();
        assertNull(sceneDelegate.getNavigationScene().requireView().getBackground());
    }

    @Test
    public void testTag() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().resume();
        TestActivity activity = controller.get();
        List<SceneDelegate> list = new ArrayList<>();
        list.add(NavigationSceneUtility.setupWithActivity(activity, TestScene.class).build());
        list.add(NavigationSceneUtility.setupWithActivity(activity, TestScene.class).tag("1").build());
        list.add(NavigationSceneUtility.setupWithActivity(activity, TestScene.class).tag("2").build());
        list.add(NavigationSceneUtility.setupWithActivity(activity, TestScene.class).tag("3").build());
        for (SceneDelegate delegate : list) {
            delegate.abandon();
        }
    }

    @Test
    public void testFixSceneWindowBackgroundEnabled() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().resume();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).fixSceneWindowBackgroundEnabled(true).build();
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
            }
        };
        sceneDelegate.getNavigationScene().push(scene);
        assertNotNull(scene.requireView().getBackground());
        sceneDelegate.abandon();

        sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).fixSceneWindowBackgroundEnabled(false).build();
        scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
            }
        };
        sceneDelegate.getNavigationScene().push(scene);
        assertNull(scene.requireView().getBackground());
    }

    public static class TestScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
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
