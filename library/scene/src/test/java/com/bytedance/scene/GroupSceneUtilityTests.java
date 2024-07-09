package com.bytedance.scene;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.group.GroupScene;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GroupSceneUtilityTests {
    @Test
    public void testSetupInActivityOnCreate() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class).build();
        GroupScene groupScene = sceneDelegate.getGroupScene();
        assertNotNull(groupScene);
        assertEquals(State.ACTIVITY_CREATED, groupScene.getState());
        controller.start();
        assertEquals(State.STARTED, groupScene.getState());
        controller.resume();
        assertEquals(State.RESUMED, groupScene.getState());
        controller.pause();
        assertEquals(State.STARTED, groupScene.getState());
        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, groupScene.getState());
        controller.destroy();
        assertEquals(State.NONE, groupScene.getState());
    }

    @LooperMode(PAUSED)
    @Test
    public void testNotImmediateSetupInActivityOnCreate() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class).immediate(false).build();
        GroupScene groupScene = sceneDelegate.getGroupScene();
        assertNotNull(groupScene);

        assertEquals(State.NONE, groupScene.getState());
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertEquals(State.ACTIVITY_CREATED, groupScene.getState());

        controller.start();
        assertEquals(State.STARTED, groupScene.getState());
        controller.resume();
        assertEquals(State.RESUMED, groupScene.getState());
        controller.pause();
        assertEquals(State.STARTED, groupScene.getState());
        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, groupScene.getState());
        controller.destroy();
        assertEquals(State.NONE, groupScene.getState());
    }

    @Test
    public void testSetupInActivityOnStart() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start();
        TestActivity activity = controller.get();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class).build();
        GroupScene groupScene = sceneDelegate.getGroupScene();
        assertNotNull(groupScene);
        assertEquals(State.STARTED, groupScene.getState());
        controller.resume();
        assertEquals(State.RESUMED, groupScene.getState());
        controller.pause();
        assertEquals(State.STARTED, groupScene.getState());
        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, groupScene.getState());
        controller.destroy();
        assertEquals(State.NONE, groupScene.getState());
    }

    @LooperMode(PAUSED)
    @Test
    public void testNotImmediateSetupInActivityOnStart() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start();
        TestActivity activity = controller.get();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class).immediate(false).build();
        GroupScene groupScene = sceneDelegate.getGroupScene();
        assertNotNull(groupScene);

        assertEquals(State.NONE, groupScene.getState());
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertEquals(State.STARTED, groupScene.getState());

        controller.resume();
        assertEquals(State.RESUMED, groupScene.getState());
        controller.pause();
        assertEquals(State.STARTED, groupScene.getState());
        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, groupScene.getState());
        controller.destroy();
        assertEquals(State.NONE, groupScene.getState());
    }

    @Test
    public void testSetupInActivityOnResume() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity activity = controller.get();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class).build();
        GroupScene groupScene = sceneDelegate.getGroupScene();
        assertNotNull(groupScene);
        assertEquals(State.RESUMED, groupScene.getState());
        controller.pause();
        assertEquals(State.STARTED, groupScene.getState());
        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, groupScene.getState());
        controller.destroy();
        assertEquals(State.NONE, groupScene.getState());
    }

    @LooperMode(PAUSED)
    @Test
    public void testNotImmediateSetupInActivityOnResume() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity activity = controller.get();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class).immediate(false).build();
        GroupScene groupScene = sceneDelegate.getGroupScene();
        assertNotNull(groupScene);

        assertEquals(State.NONE, groupScene.getState());
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertEquals(State.RESUMED, groupScene.getState());

        controller.pause();
        assertEquals(State.STARTED, groupScene.getState());
        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, groupScene.getState());
        controller.destroy();
        assertEquals(State.NONE, groupScene.getState());
    }

    @Test
    public void testSetupInActivityOnPause() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume().pause();
        TestActivity activity = controller.get();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class).build();
        GroupScene groupScene = sceneDelegate.getGroupScene();
        assertNotNull(groupScene);
        assertEquals(State.STARTED, groupScene.getState());
        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, groupScene.getState());
        controller.destroy();
        assertEquals(State.NONE, groupScene.getState());
    }

    @LooperMode(PAUSED)
    @Test
    public void testNotImmediateSetupInActivityOnPause() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume().pause();
        TestActivity activity = controller.get();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class).immediate(false).build();
        GroupScene groupScene = sceneDelegate.getGroupScene();
        assertNotNull(groupScene);

        assertEquals(State.NONE, groupScene.getState());
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertEquals(State.STARTED, groupScene.getState());

        controller.stop();
        assertEquals(State.ACTIVITY_CREATED, groupScene.getState());
        controller.destroy();
        assertEquals(State.NONE, groupScene.getState());
    }

    @Test
    public void testSetupInActivityOnStop() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume().pause().stop();
        TestActivity activity = controller.get();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class).build();
        GroupScene groupScene = sceneDelegate.getGroupScene();
        assertNotNull(groupScene);
        assertEquals(State.ACTIVITY_CREATED, groupScene.getState());
        controller.destroy();
        assertEquals(State.NONE, groupScene.getState());
    }

    @LooperMode(PAUSED)
    @Test
    public void testNotImmediateSetupInActivityOnStop() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume().pause().stop();
        TestActivity activity = controller.get();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class).immediate(false).build();
        GroupScene groupScene = sceneDelegate.getGroupScene();
        assertNotNull(groupScene);

        assertEquals(State.NONE, groupScene.getState());
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertEquals(State.ACTIVITY_CREATED, groupScene.getState());
        controller.destroy();
        assertEquals(State.NONE, groupScene.getState());
    }

    //api16 can't known whether Activity is destroyed
    @Config(minSdk = Build.VERSION_CODES.JELLY_BEAN_MR1, maxSdk = Build.VERSION_CODES.P)
    @Test
    public void testSetupInActivityOnDestroy() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume().pause().stop().destroy();
        TestActivity activity = controller.get();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class).build();
        GroupScene groupScene = sceneDelegate.getGroupScene();
        assertNotNull(groupScene);
        assertEquals(State.NONE, groupScene.getState());
    }

    //api16 can't known whether Activity is destroyed
    @Config(minSdk = Build.VERSION_CODES.JELLY_BEAN_MR1, maxSdk = Build.VERSION_CODES.P)
    @LooperMode(PAUSED)
    @Test
    public void testNotImmediateSetupInActivityOnDestroy() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume().pause().stop().destroy();
        TestActivity activity = controller.get();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class).immediate(false).build();
        GroupScene groupScene = sceneDelegate.getGroupScene();
        assertNotNull(groupScene);

        assertEquals(State.NONE, groupScene.getState());
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertEquals(State.NONE, groupScene.getState());
    }

    @Test
    public void testRootSceneComponentFactory() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        final TestScene scene = new TestScene();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class)
                .rootSceneComponentFactory(new SceneComponentFactory() {
                    @Override
                    public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                        return scene;
                    }
                }).build();
        controller.start().resume();
        assertEquals(scene, sceneDelegate.getGroupScene());
    }

    @Test
    public void testRootSceneInstance() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        final TestScene scene = new TestScene();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class)
                .rootScene(scene).build();
        controller.start().resume();
        assertEquals(scene, sceneDelegate.getGroupScene());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRootSceneInstanceTypeError() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class)
                .rootScene(new GroupScene() {
                    @NonNull
                    @Override
                    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                        return new FrameLayout(requireSceneContext());
                    }
                }).build();
    }

    @Test
    public void testAbandon() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        GroupSceneDelegate sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class).build();
        controller.start().resume();
        GroupScene groupScene = sceneDelegate.getGroupScene();
        View groupSceneView = groupScene.requireView();
        sceneDelegate.abandon();
        assertNull(groupSceneView.getParent());
        sceneDelegate = GroupSceneUtility.setupWithActivity(activity, TestScene.class).build();
        sceneDelegate.abandon();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTagDuplicateException() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create();
        TestActivity activity = controller.get();
        GroupSceneUtility.setupWithActivity(activity, TestScene.class).build();
        controller.start().resume();
        GroupSceneUtility.setupWithActivity(activity, TestScene.class).build();
    }

    @Test
    public void testTag() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().resume();
        TestActivity activity = controller.get();
        List<GroupSceneDelegate> list = new ArrayList<>();
        list.add(GroupSceneUtility.setupWithActivity(activity, TestScene.class).build());
        list.add(GroupSceneUtility.setupWithActivity(activity, TestScene.class).tag("1").build());
        list.add(GroupSceneUtility.setupWithActivity(activity, TestScene.class).tag("2").build());
        list.add(GroupSceneUtility.setupWithActivity(activity, TestScene.class).tag("3").build());
        for (GroupSceneDelegate delegate : list) {
            delegate.abandon();
        }
    }

    public static class TestScene extends GroupScene {
        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new FrameLayout(requireSceneContext());
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
