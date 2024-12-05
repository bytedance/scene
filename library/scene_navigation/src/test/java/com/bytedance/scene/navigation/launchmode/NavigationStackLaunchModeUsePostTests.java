package com.bytedance.scene.navigation.launchmode;

import static android.os.Looper.getMainLooper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

import android.app.Activity;
import android.content.res.Configuration;
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
import com.bytedance.scene.State;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.launchmode.LaunchMode;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSourceUtility;
import com.bytedance.scene.utlity.ViewIdGenerator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

/**
 * Push Pop PopTo PopToRoot PushOptions LaunchMode tests + UsePost
 **/

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NavigationStackLaunchModeUsePostTests {
    @Test
    @LooperMode(PAUSED)
    public void testStandard() {
        final TestChildScene rootScene = new TestChildScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        assertEquals(rootScene.getState(), State.RESUMED);
        assertFalse(rootScene.mOnNewIntentInvoked);

        TestChildScene scene0 = new TestChildScene();

        assertEquals(scene0.getState(), State.NONE);
        assertEquals(rootScene.getState(), State.RESUMED);

        navigationScene.push(scene0, new PushOptions.Builder().setLaunchMode(LaunchMode.STANDARD).setUsePost(true).setUsePostWhenPause(true).build());

        assertEquals(scene0.getState(), State.NONE);
        assertFalse(scene0.mOnNewIntentInvoked);
        assertFalse(rootScene.mOnNewIntentInvoked);

        shadowOf(getMainLooper()).runToEndOfTasks();//execute Handler posted task

        assertFalse(scene0.mOnNewIntentInvoked);
        assertEquals(scene0.getState(), State.RESUMED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);
        assertFalse(rootScene.mOnNewIntentInvoked);

        navigationScene.pop();
        assertEquals(scene0.getState(), State.NONE);
        assertEquals(rootScene.getState(), State.RESUMED);
    }

    @Test
    @LooperMode(PAUSED)
    public void testPushSingleTop() {
        final TestScene groupScene = new TestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        TestChildScene secondScene = new TestChildScene();

        navigationScene.push(secondScene);

        assertEquals(secondScene.getState(), State.RESUMED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(2, navigationScene.getSceneList().size());
        assertFalse(secondScene.mOnNewIntentInvoked);
        assertNull(secondScene.mOnNewIntentArguments);

        TestChildScene thirdScene = new TestChildScene();
        Bundle arguments = new Bundle();
        thirdScene.setArguments(arguments);
        navigationScene.push(thirdScene, new PushOptions.Builder().setLaunchMode(LaunchMode.SINGLE_TOP).setUsePost(true).setUsePostWhenPause(true).build());

        assertEquals(thirdScene.getState(), State.NONE);
        assertFalse(secondScene.mOnNewIntentInvoked);
        assertNull(secondScene.mOnNewIntentArguments);

        shadowOf(getMainLooper()).runToEndOfTasks();//execute Handler posted task

        //Third Scene will be skipped
        assertEquals(thirdScene.getState(), State.NONE);

        assertEquals(secondScene.getState(), State.RESUMED);
        assertTrue(secondScene.mOnNewIntentInvoked);
        assertEquals(secondScene.mOnNewIntentArguments, arguments);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(2, navigationScene.getSceneList().size());
        assertEquals(secondScene, navigationScene.getCurrentScene());

        navigationScene.push(new TestScene());
        navigationScene.push(new TestScene());
        navigationScene.push(new TestScene());
        navigationScene.push(new TestScene());
        assertEquals(6, navigationScene.getSceneList().size());

        TestChildScene newTopScene = new TestChildScene();
        navigationScene.push(newTopScene, new PushOptions.Builder().setLaunchMode(LaunchMode.SINGLE_TOP).setUsePost(true).setUsePostWhenPause(true).build());
        shadowOf(getMainLooper()).runToEndOfTasks();//execute Handler posted task
        assertEquals(7, navigationScene.getSceneList().size());
        assertEquals(newTopScene.getState(), State.RESUMED);
        assertFalse(newTopScene.mOnNewIntentInvoked);
        assertNull(newTopScene.mOnNewIntentArguments);
    }

    @Test
    @LooperMode(PAUSED)
    public void testPushSingleTask() {
        final TestScene groupScene = new TestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        navigationScene.push(TestChildScene.class);
        assertEquals(2, navigationScene.getSceneList().size());

        for (int i = 0; i < 100; i++) {
            navigationScene.push(CountChildScene.class);
        }
        assertEquals(102, navigationScene.getSceneList().size());

        navigationScene.push(TestChildScene.class, null, new PushOptions.Builder().setLaunchMode(LaunchMode.SINGLE_TASK).setUsePost(true).setUsePostWhenPause(true).build());

        assertEquals(102, navigationScene.getSceneList().size());
        assertFalse(((TestChildScene) navigationScene.getSceneList().get(1)).mOnNewIntentInvoked);

        shadowOf(getMainLooper()).runToEndOfTasks();//execute Handler posted task

        assertEquals(2, navigationScene.getSceneList().size());
        assertTrue(((TestChildScene) navigationScene.getSceneList().get(1)).mOnNewIntentInvoked);

        ((TestChildScene) navigationScene.getSceneList().get(1)).mOnNewIntentInvoked = false;
        navigationScene.push(TestChildScene.class, null, new PushOptions.Builder().setLaunchMode(LaunchMode.SINGLE_TASK).setUsePost(true).setUsePostWhenPause(true).build());
        shadowOf(getMainLooper()).runToEndOfTasks();//execute Handler posted task
        assertTrue(((TestChildScene) navigationScene.getSceneList().get(1)).mOnNewIntentInvoked);
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
    }

    public static class TestChildScene extends Scene implements ActivityCompatibleBehavior {
        boolean mOnNewIntentInvoked = false;
        Bundle mOnNewIntentArguments = null;

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }

        @Override
        public void onConfigurationChanged(@NonNull Configuration newConfig) {

        }

        @Override
        public void onNewIntent(@Nullable @org.jetbrains.annotations.Nullable Bundle arguments) {
            this.mOnNewIntentInvoked = true;
            this.mOnNewIntentArguments = arguments;
        }

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {

        }
    }

    public static class CountChildScene extends Scene {
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
