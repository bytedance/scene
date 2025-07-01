package com.bytedance.scene.navigation;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.bytedance.scene.*;
import com.bytedance.scene.navigation.NavigationScene;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.util.ArrayList;
import java.util.List;

import static android.os.Looper.getMainLooper;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NavigationSceneUtilityTests {
    @Test
    public void testSetupInActivityOnCreate() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).build();
        NavigationScene navigationScene = sceneDelegate.getNavigationScene();
        assertNotNull(navigationScene);
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getState());
        controller.start();
        assertEquals(State.STARTED, navigationScene.getState());
        controller.resume();
        assertEquals(State.RESUMED, navigationScene.getState());
        controller.pause();
        assertEquals(State.STARTED, navigationScene.getState());
        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getState());
        controller.destroy();
        assertEquals(State.NONE, navigationScene.getState());
    }

    @LooperMode(PAUSED)
    @Test
    public void testNotImmediateSetupInActivityOnCreate() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).immediate(false).build();
        NavigationScene navigationScene = sceneDelegate.getNavigationScene();
        assertNotNull(navigationScene);

        assertEquals(State.NONE, navigationScene.getState());
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getState());

        controller.start();
        assertEquals(State.STARTED, navigationScene.getState());
        controller.resume();
        assertEquals(State.RESUMED, navigationScene.getState());
        controller.pause();
        assertEquals(State.STARTED, navigationScene.getState());
        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getState());
        controller.destroy();
        assertEquals(State.NONE, navigationScene.getState());
    }

    @Test
    public void testSetupInActivityOnStart() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).build();
        NavigationScene navigationScene = sceneDelegate.getNavigationScene();
        assertNotNull(navigationScene);
        assertEquals(State.STARTED, navigationScene.getState());
        controller.resume();
        assertEquals(State.RESUMED, navigationScene.getState());
        controller.pause();
        assertEquals(State.STARTED, navigationScene.getState());
        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getState());
        controller.destroy();
        assertEquals(State.NONE, navigationScene.getState());
    }

    @LooperMode(PAUSED)
    @Test
    public void testNotImmediateSetupInActivityOnStart() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).immediate(false).build();
        NavigationScene navigationScene = sceneDelegate.getNavigationScene();
        assertNotNull(navigationScene);

        assertEquals(State.NONE, navigationScene.getState());
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertEquals(State.STARTED, navigationScene.getState());

        controller.resume();
        assertEquals(State.RESUMED, navigationScene.getState());
        controller.pause();
        assertEquals(State.STARTED, navigationScene.getState());
        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getState());
        controller.destroy();
        assertEquals(State.NONE, navigationScene.getState());
    }

    @Test
    public void testSetupInActivityOnResume() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).build();
        NavigationScene navigationScene = sceneDelegate.getNavigationScene();
        assertNotNull(navigationScene);
        assertEquals(State.RESUMED, navigationScene.getState());
        controller.pause();
        assertEquals(State.STARTED, navigationScene.getState());
        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getState());
        controller.destroy();
        assertEquals(State.NONE, navigationScene.getState());
    }

    @LooperMode(PAUSED)
    @Test
    public void testNotImmediateSetupInActivityOnResume() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).immediate(false).build();
        NavigationScene navigationScene = sceneDelegate.getNavigationScene();
        assertNotNull(navigationScene);

        assertEquals(State.NONE, navigationScene.getState());
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertEquals(State.RESUMED, navigationScene.getState());

        controller.pause();
        assertEquals(State.STARTED, navigationScene.getState());
        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getState());
        controller.destroy();
        assertEquals(State.NONE, navigationScene.getState());
    }

    @Test
    public void testSetupInActivityOnPause() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume().pause();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).build();
        NavigationScene navigationScene = sceneDelegate.getNavigationScene();
        assertNotNull(navigationScene);
        assertEquals(State.STARTED, navigationScene.getState());
        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getState());
        controller.destroy();
        assertEquals(State.NONE, navigationScene.getState());
    }

    @LooperMode(PAUSED)
    @Test
    public void testNotImmediateSetupInActivityOnPause() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume().pause();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).immediate(false).build();
        NavigationScene navigationScene = sceneDelegate.getNavigationScene();
        assertNotNull(navigationScene);

        assertEquals(State.NONE, navigationScene.getState());
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertEquals(State.STARTED, navigationScene.getState());

        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getState());
        controller.destroy();
        assertEquals(State.NONE, navigationScene.getState());
    }

    @Test
    public void testSetupInActivityOnStop() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume().pause().stop();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).build();
        NavigationScene navigationScene = sceneDelegate.getNavigationScene();
        assertNotNull(navigationScene);
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getState());
        controller.destroy();
        assertEquals(State.NONE, navigationScene.getState());
    }

    @LooperMode(PAUSED)
    @Test
    public void testNotImmediateSetupInActivityOnStop() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume().pause().stop();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).immediate(false).build();
        NavigationScene navigationScene = sceneDelegate.getNavigationScene();
        assertNotNull(navigationScene);

        assertEquals(State.NONE, navigationScene.getState());
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getState());
        controller.destroy();
        assertEquals(State.NONE, navigationScene.getState());
    }

    //api16 can't known whether Activity is destroyed
    @Config(minSdk = Build.VERSION_CODES.KITKAT, maxSdk = Build.VERSION_CODES.O_MR1)
    @Test
    public void testSetupInActivityOnDestroy() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume().pause().stop().destroy();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).build();
        NavigationScene navigationScene = sceneDelegate.getNavigationScene();
        assertNotNull(navigationScene);
        assertEquals(State.NONE, navigationScene.getState());
    }

    //api16 can't known whether Activity is destroyed
    @Config(minSdk = Build.VERSION_CODES.KITKAT, maxSdk = Build.VERSION_CODES.P)
    @LooperMode(PAUSED)
    @Test
    public void testNotImmediateSetupInActivityOnDestroy() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume().pause().stop().destroy();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).immediate(false).build();
        NavigationScene navigationScene = sceneDelegate.getNavigationScene();
        assertNotNull(navigationScene);

        assertEquals(State.NONE, navigationScene.getState());
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertEquals(State.NONE, navigationScene.getState());
    }

    @Test
    public void testRootSceneComponentFactory() {
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
    public void testRootSceneInstance() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        final TestScene scene = new TestScene();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class)
                .rootScene(scene).build();
        controller.start().resume();
        assertEquals(scene, sceneDelegate.getNavigationScene().getCurrentScene());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRootSceneInstanceTypeError() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class)
                .rootScene(new Scene() {
                    @NonNull
                    @Override
                    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                        return new View(requireSceneContext());
                    }
                }).build();
    }

    @Test
    public void testAbandon() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        SceneDelegate sceneDelegate = NavigationSceneUtility.setupWithActivity(activity, TestScene.class).build();
        controller.start().resume();
        NavigationScene navigationScene = sceneDelegate.getNavigationScene();
        View navigationSceneView = navigationScene.requireView();
        sceneDelegate.abandon();
        assertNull(navigationSceneView.getParent());
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
