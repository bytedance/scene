package com.bytedance.scene.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneDelegate;
import com.bytedance.scene.State;
import com.bytedance.scene.navigation.NavigationScene;
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
public class NavigationSceneCompatUtility_SetupWithFragment_Tests {
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

            SceneDelegate sceneDelegate = NavigationSceneCompatUtility.setupWithFragment(this, TestScene.class, mId).tag("onViewCreated").build();
            NavigationScene navigationScene = sceneDelegate.getNavigationScene();
            assertNotNull(navigationScene);
            assertEquals(State.NONE, navigationScene.getState());
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mMethodInvokedCount++;

            SceneDelegate sceneDelegate = NavigationSceneCompatUtility.setupWithFragment(this, TestScene.class, mId).tag("onActivityCreated").build();
            NavigationScene navigationScene = sceneDelegate.getNavigationScene();
            assertNotNull(navigationScene);
            assertEquals(State.NONE, navigationScene.getState());
        }

        @Override
        public void onStart() {
            super.onStart();
            mMethodInvokedCount++;

            SceneDelegate sceneDelegate = NavigationSceneCompatUtility.setupWithFragment(this, TestScene.class, mId).tag("onStart").build();
            NavigationScene navigationScene = sceneDelegate.getNavigationScene();
            assertNotNull(navigationScene);
            assertEquals(State.ACTIVITY_CREATED, navigationScene.getState());
        }

        @Override
        public void onResume() {
            super.onResume();
            mMethodInvokedCount++;

            SceneDelegate sceneDelegate = NavigationSceneCompatUtility.setupWithFragment(this, TestScene.class, mId).tag("onResume").build();
            NavigationScene navigationScene = sceneDelegate.getNavigationScene();
            assertNotNull(navigationScene);
            assertEquals(State.STARTED, navigationScene.getState());
        }

        @Override
        public void onPause() {
            super.onPause();
            mMethodInvokedCount++;

            SceneDelegate sceneDelegate = NavigationSceneCompatUtility.setupWithFragment(this, TestScene.class, mId).tag("onPause").build();
            NavigationScene navigationScene = sceneDelegate.getNavigationScene();
            assertNotNull(navigationScene);
            assertEquals(State.STARTED, navigationScene.getState());
        }

        @Override
        public void onStop() {
            super.onStop();
            mMethodInvokedCount++;

            SceneDelegate sceneDelegate = NavigationSceneCompatUtility.setupWithFragment(this, TestScene.class, mId).tag("onStop").build();
            NavigationScene navigationScene = sceneDelegate.getNavigationScene();
            assertNotNull(navigationScene);
            assertEquals(State.ACTIVITY_CREATED, navigationScene.getState());
        }
    }

    public static class TestScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }
}