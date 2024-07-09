package com.bytedance.scene.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bytedance.scene.GroupSceneDelegate;
import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.SceneDelegate;
import com.bytedance.scene.State;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.ui.utility.TestAppCompatActivity;
import com.bytedance.scene.utlity.ViewIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GroupSceneCompatUtility_SetupWithFragment_Tests {
    @Test
    public void testSetupInFragmentLifecycleMethods_Fragment_Added_In_Activity_OnCreate() {
        ActivityController<TestAppCompatActivity> controller = Robolectric.buildActivity(TestAppCompatActivity.class).create();
        TestAppCompatActivity activity = controller.get();
        TestNormalFragment_Add_In_Activity_OnCreate testFragment = new TestNormalFragment_Add_In_Activity_OnCreate();
        activity.getSupportFragmentManager().beginTransaction().add(activity.mFrameLayout.getId(), testFragment).commitNowAllowingStateLoss();
        controller.start();
        controller.resume();
        controller.pause();
        controller.stop();
        controller.destroy();
        assertEquals(6, testFragment.mMethodInvokedCount);
    }

    @Test
    public void testRootSceneComponentFactory() {
        ActivityController<TestAppCompatActivity> controller = Robolectric.buildActivity(TestAppCompatActivity.class).create();
        TestAppCompatActivity activity = controller.get();

        TestFragment testFragment = new TestFragment();
        activity.getSupportFragmentManager().beginTransaction().add(activity.mFrameLayout.getId(), testFragment).commitNowAllowingStateLoss();
        controller.start();
        controller.resume();

        final TestScene scene = new TestScene();
        GroupSceneDelegate sceneDelegate = GroupSceneCompatUtility.setupWithFragment(testFragment, TestScene.class, testFragment.mId)
                .rootSceneComponentFactory(new SceneComponentFactory() {
                    @Override
                    public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                        return scene;
                    }
                }).build();
        assertEquals(scene, sceneDelegate.getGroupScene());
    }

    @Test
    public void testRootSceneInstance() {
        ActivityController<TestAppCompatActivity> controller = Robolectric.buildActivity(TestAppCompatActivity.class).create();
        TestAppCompatActivity activity = controller.get();

        TestFragment testFragment = new TestFragment();
        activity.getSupportFragmentManager().beginTransaction().add(activity.mFrameLayout.getId(), testFragment).commitNowAllowingStateLoss();
        controller.start();
        controller.resume();

        final TestScene scene = new TestScene();
        GroupSceneDelegate sceneDelegate = GroupSceneCompatUtility.setupWithFragment(testFragment, TestScene.class, testFragment.mId)
                .rootScene(scene).build();
        assertEquals(scene, sceneDelegate.getGroupScene());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRootSceneInstanceTypeError() {
        ActivityController<TestAppCompatActivity> controller = Robolectric.buildActivity(TestAppCompatActivity.class).create();
        TestAppCompatActivity activity = controller.get();

        TestFragment testFragment = new TestFragment();
        activity.getSupportFragmentManager().beginTransaction().add(activity.mFrameLayout.getId(), testFragment).commitNowAllowingStateLoss();
        controller.start();
        controller.resume();

        GroupSceneDelegate sceneDelegate = GroupSceneCompatUtility.setupWithFragment(testFragment, TestScene.class, testFragment.mId)
                .rootScene(new GroupScene() {
                    @NonNull
                    @Override
                    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                        return new FrameLayout(requireSceneContext());
                    }
                }).build();
    }

    public static class TestNormalFragment_Add_In_Activity_OnCreate extends Fragment {
        private int mId = ViewIdGenerator.generateViewId();
        public int mMethodInvokedCount = 0;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            FrameLayout layout = new FrameLayout(requireActivity());
            layout.setId(mId);
            return layout;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mMethodInvokedCount++;

            GroupSceneDelegate sceneDelegate = GroupSceneCompatUtility.setupWithFragment(this, TestScene.class, mId).tag("onViewCreated").build();
            GroupScene groupScene = sceneDelegate.getGroupScene();
            assertNotNull(groupScene);
            assertEquals(State.NONE, groupScene.getState());
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mMethodInvokedCount++;

            GroupSceneDelegate sceneDelegate = GroupSceneCompatUtility.setupWithFragment(this, TestScene.class, mId).tag("onActivityCreated").build();
            GroupScene groupScene = sceneDelegate.getGroupScene();
            assertNotNull(groupScene);
            assertEquals(State.NONE, groupScene.getState());
        }

        @Override
        public void onStart() {
            super.onStart();
            mMethodInvokedCount++;

            GroupSceneDelegate sceneDelegate = GroupSceneCompatUtility.setupWithFragment(this, TestScene.class, mId).tag("onStart").build();
            GroupScene groupScene = sceneDelegate.getGroupScene();
            assertNotNull(groupScene);
            assertEquals(State.ACTIVITY_CREATED, groupScene.getState());
        }

        @Override
        public void onResume() {
            super.onResume();
            mMethodInvokedCount++;

            GroupSceneDelegate sceneDelegate = GroupSceneCompatUtility.setupWithFragment(this, TestScene.class, mId).tag("onResume").build();
            GroupScene groupScene = sceneDelegate.getGroupScene();
            assertNotNull(groupScene);
            assertEquals(State.STARTED, groupScene.getState());
        }

        @Override
        public void onPause() {
            super.onPause();
            mMethodInvokedCount++;

            GroupSceneDelegate sceneDelegate = GroupSceneCompatUtility.setupWithFragment(this, TestScene.class, mId).tag("onPause").build();
            GroupScene groupScene = sceneDelegate.getGroupScene();
            assertNotNull(groupScene);
            assertEquals(State.STARTED, groupScene.getState());
        }

        @Override
        public void onStop() {
            super.onStop();
            mMethodInvokedCount++;

            GroupSceneDelegate sceneDelegate = GroupSceneCompatUtility.setupWithFragment(this, TestScene.class, mId).tag("onStop").build();
            GroupScene groupScene = sceneDelegate.getGroupScene();
            assertNotNull(groupScene);
            assertEquals(State.ACTIVITY_CREATED, groupScene.getState());
        }
    }

    public static class TestScene extends GroupScene {
        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new FrameLayout(requireSceneContext());
        }
    }

    public static class TestFragment extends Fragment{
        final int mId = ViewIdGenerator.generateViewId();
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            FrameLayout layout = new FrameLayout(requireActivity());
            layout.setId(mId);
            return layout;
        }
    }
}