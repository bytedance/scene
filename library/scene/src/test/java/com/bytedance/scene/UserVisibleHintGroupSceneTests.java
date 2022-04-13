package com.bytedance.scene;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.Lifecycle;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scene.utility.TestUtility;
import com.bytedance.scene.utlity.ViewIdGenerator;
import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class UserVisibleHintGroupSceneTests {
    @Test
    public void testUserVisibleHintLifecycleSyncWithSceneLifecycle() {
        TestScene testScene = new TestScene();
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(testScene);

        SceneLifecycleManager<GroupScene> sceneLifecycleManager = pair.first;

        Truth.assertThat(testScene.getUserVisibleHintLifecycle().getCurrentState()).isEqualTo(Lifecycle.State.CREATED);
        Truth.assertThat(testScene.isVisible()).isFalse();

        sceneLifecycleManager.onStart();
        Truth.assertThat(testScene.getUserVisibleHintLifecycle().getCurrentState()).isEqualTo(Lifecycle.State.STARTED);
        Truth.assertThat(testScene.isVisible()).isTrue();

        sceneLifecycleManager.onResume();
        Truth.assertThat(testScene.getUserVisibleHintLifecycle().getCurrentState()).isEqualTo(Lifecycle.State.RESUMED);
        Truth.assertThat(testScene.isVisible()).isTrue();

        sceneLifecycleManager.onPause();
        Truth.assertThat(testScene.getUserVisibleHintLifecycle().getCurrentState()).isEqualTo(Lifecycle.State.STARTED);
        Truth.assertThat(testScene.isVisible()).isTrue();

        sceneLifecycleManager.onStop();
        Truth.assertThat(testScene.getUserVisibleHintLifecycle().getCurrentState()).isEqualTo(Lifecycle.State.CREATED);
        Truth.assertThat(testScene.isVisible()).isFalse();

        sceneLifecycleManager.onDestroyView();
        Truth.assertThat(testScene.getUserVisibleHintLifecycle().getCurrentState()).isEqualTo(Lifecycle.State.DESTROYED);
        Truth.assertThat(testScene.isVisible()).isFalse();
    }

    @Test
    public void testSetUserVisibleHint() {
        TestScene testScene = new TestScene();
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(testScene);

        SceneLifecycleManager<GroupScene> sceneLifecycleManager = pair.first;
        Truth.assertThat(testScene.getUserVisibleHint()).isTrue();

        sceneLifecycleManager.onStart();
        Truth.assertThat(testScene.getUserVisibleHint()).isTrue();
        Truth.assertThat(testScene.isVisible()).isTrue();

        testScene.setUserVisibleHint(false);
        Truth.assertThat(testScene.getUserVisibleHint()).isFalse();
        Truth.assertThat(testScene.isVisible()).isFalse();

        testScene.setUserVisibleHint(true);
        Truth.assertThat(testScene.getUserVisibleHint()).isTrue();
        Truth.assertThat(testScene.isVisible()).isTrue();

        testScene.setUserVisibleHint(false);

        sceneLifecycleManager.onResume();
        Truth.assertThat(testScene.getUserVisibleHint()).isFalse();
        Truth.assertThat(testScene.isVisible()).isFalse();

        sceneLifecycleManager.onPause();
        Truth.assertThat(testScene.getUserVisibleHint()).isFalse();
        Truth.assertThat(testScene.isVisible()).isFalse();

        sceneLifecycleManager.onStop();
        Truth.assertThat(testScene.getUserVisibleHint()).isFalse();
        Truth.assertThat(testScene.isVisible()).isFalse();

        sceneLifecycleManager.onDestroyView();
        Truth.assertThat(testScene.getUserVisibleHint()).isFalse();
        Truth.assertThat(testScene.isVisible()).isFalse();
    }

    @Test
    public void test() {
        TestScene testScene = new TestScene();
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(testScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;

        GroupSceneLifecycleTests.TestChildScene childScene = new GroupSceneLifecycleTests.TestChildScene();
        assertEquals(childScene.getState(), State.NONE);

        testScene.add(testScene.mId, childScene, "childScene");
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);

        sceneLifecycleManager.onStart();
        assertEquals(childScene.getState(), State.STARTED);
        assertTrue(testScene.isVisible());

        testScene.setUserVisibleHint(false);
        assertEquals(childScene.getState(), State.STARTED);
        assertFalse(testScene.isVisible());
        assertFalse(testScene.getUserVisibleHint());
        assertEquals(testScene.getLifecycle().getCurrentState(), Lifecycle.State.STARTED);
        assertEquals(testScene.getUserVisibleHintLifecycle().getCurrentState(), Lifecycle.State.CREATED);

        testScene.setUserVisibleHint(true);
        assertEquals(childScene.getState(), State.STARTED);
        assertTrue(testScene.isVisible());
        assertTrue(testScene.getUserVisibleHint());
        assertEquals(testScene.getLifecycle().getCurrentState(), Lifecycle.State.STARTED);
        assertEquals(testScene.getUserVisibleHintLifecycle().getCurrentState(), Lifecycle.State.STARTED);

        sceneLifecycleManager.onResume();
        assertEquals(childScene.getState(), State.RESUMED);
        assertTrue(testScene.isVisible());
        assertEquals(testScene.getLifecycle().getCurrentState(), Lifecycle.State.RESUMED);
        assertEquals(testScene.getUserVisibleHintLifecycle().getCurrentState(), Lifecycle.State.RESUMED);

        testScene.setUserVisibleHint(false);
        assertEquals(childScene.getState(), State.RESUMED);
        assertFalse(testScene.isVisible());
        assertFalse(testScene.getUserVisibleHint());
        assertEquals(testScene.getLifecycle().getCurrentState(), Lifecycle.State.RESUMED);
        assertEquals(testScene.getUserVisibleHintLifecycle().getCurrentState(), Lifecycle.State.CREATED);

        testScene.setUserVisibleHint(true);
        assertEquals(childScene.getState(), State.RESUMED);
        assertTrue(testScene.isVisible());
        assertTrue(testScene.getUserVisibleHint());
        assertEquals(testScene.getLifecycle().getCurrentState(), Lifecycle.State.RESUMED);
        assertEquals(testScene.getUserVisibleHintLifecycle().getCurrentState(), Lifecycle.State.RESUMED);

        sceneLifecycleManager.onPause();
        assertEquals(childScene.getState(), State.STARTED);
        assertTrue(testScene.isVisible());

        testScene.setUserVisibleHint(false);
        assertEquals(childScene.getState(), State.STARTED);
        assertFalse(testScene.isVisible());
        assertFalse(testScene.getUserVisibleHint());
        assertEquals(testScene.getLifecycle().getCurrentState(), Lifecycle.State.STARTED);
        assertEquals(testScene.getUserVisibleHintLifecycle().getCurrentState(), Lifecycle.State.CREATED);

        testScene.setUserVisibleHint(true);
        assertEquals(childScene.getState(), State.STARTED);
        assertTrue(testScene.isVisible());
        assertTrue(testScene.getUserVisibleHint());
        assertEquals(testScene.getLifecycle().getCurrentState(), Lifecycle.State.STARTED);
        assertEquals(testScene.getUserVisibleHintLifecycle().getCurrentState(), Lifecycle.State.STARTED);

        sceneLifecycleManager.onStop();
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);

        testScene.setUserVisibleHint(false);
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertFalse(testScene.isVisible());
        assertFalse(testScene.getUserVisibleHint());
        assertEquals(testScene.getLifecycle().getCurrentState(), Lifecycle.State.CREATED);
        assertEquals(testScene.getUserVisibleHintLifecycle().getCurrentState(), Lifecycle.State.CREATED);

        testScene.setUserVisibleHint(true);
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertFalse(testScene.isVisible());
        assertTrue(testScene.getUserVisibleHint());

        sceneLifecycleManager.onDestroyView();
        assertEquals(childScene.getState(), State.NONE);

        testScene.setUserVisibleHint(false);
        assertEquals(childScene.getState(), State.NONE);
        assertFalse(testScene.isVisible());
        assertFalse(testScene.getUserVisibleHint());

        testScene.setUserVisibleHint(true);
        assertEquals(childScene.getState(), State.NONE);
        assertFalse(testScene.isVisible());
        assertTrue(testScene.getUserVisibleHint());

        assertNotNull(testScene.getUserVisibleHintLifecycleOwner());
    }

    /**
     * androidx.lifecycle:lifecycle-runtime:2.1.0 exception
     * <p>
     * LifecycleOwner of this LifecycleRegistry is alreadygarbage collected. It is too late to change lifecycle state.
     * java.lang.IllegalStateException: LifecycleOwner of this LifecycleRegistry is alreadygarbage collected. It is too late to change lifecycle state.
     * at androidx.lifecycle.LifecycleRegistry.sync(LifecycleRegistry.java:327)
     * at androidx.lifecycle.LifecycleRegistry.moveToState(LifecycleRegistry.java:145)
     * at androidx.lifecycle.LifecycleRegistry.handleLifecycleEvent(LifecycleRegistry.java:131)
     * at com.bytedance.scene.group.UserVisibleHintGroupScene.onActivityCreated(UserVisibleHintGroupScene.java:95)
     */
    @Test
    public void testUserVisibleHintLifecycleGC() {
        TestScene testScene = new TestScene();
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(testScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        TestUtility.forceGc();
        testScene.setUserVisibleHint(false);
    }

    public static class TestScene extends UserVisibleHintGroupScene {
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
}
