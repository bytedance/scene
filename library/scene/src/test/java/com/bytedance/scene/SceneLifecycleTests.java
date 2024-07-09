package com.bytedance.scene;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.Lifecycle;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.SceneViewTreeLifecycleOwner;
import com.bytedance.scene.utlity.SceneViewTreeSavedStateRegistryOwner;
import com.bytedance.scene.utlity.SceneViewTreeViewModelStoreOwner;
import com.bytedance.scene.utlity.ViewUtility;
import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SceneLifecycleTests {

    @Test
    public void testSceneLifecycle() {
        final TestScene testScene = new TestScene();

        assertNull(testScene.getView());
        assertNull(testScene.getActivity());
        assertNull(testScene.getParentScene());
        assertNull(testScene.getApplicationContext());
        assertEquals(testScene.getLifecycle().getCurrentState(), Lifecycle.State.INITIALIZED);
        assertEquals(testScene.getState(), State.NONE);
        assertFalse(testScene.isVisible());

        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(testScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;

        assertNotNull(testScene.getView());
        assertEquals(SceneViewTreeLifecycleOwner.get(testScene.getView()), testScene);
        Truth.assertThat(SceneViewTreeViewModelStoreOwner.get(testScene.requireView())).isEqualTo(testScene);
        Truth.assertThat(SceneViewTreeSavedStateRegistryOwner.get(testScene.requireView())).isEqualTo(testScene);
        assertNotNull(testScene.getActivity());
        assertNotNull(testScene.getApplicationContext());
        assertNotNull(testScene.getResources());
        assertNotNull(testScene.getLayoutInflater());
        assertNotNull(testScene.getParentScene());
        assertEquals(testScene.getLifecycle().getCurrentState(), Lifecycle.State.CREATED);
        assertEquals(testScene.getState(), State.ACTIVITY_CREATED);
        assertTrue(testScene.getStateHistory().contains(State.ACTIVITY_CREATED.getName()));

        assertEquals(testScene.getView().getContext().getSystemService(Scene.SCENE_SERVICE), testScene);
        assertEquals(ViewUtility.findSceneByView(testScene.getView()), testScene);
        assertFalse(testScene.isVisible());

        testScene.requireView();
        testScene.requireParentScene();
        testScene.requireActivity();
        testScene.requireSceneContext();
        testScene.requireApplicationContext();

        sceneLifecycleManager.onStart();
        assertEquals(testScene.getLifecycle().getCurrentState(), Lifecycle.State.STARTED);
        assertEquals(testScene.getState(), State.STARTED);
        assertTrue(testScene.isVisible());
        assertEquals(testScene.getView().getVisibility(), View.VISIBLE);
        assertTrue(testScene.getStateHistory().contains(State.STARTED.getName()));

        sceneLifecycleManager.onResume();
        assertEquals(testScene.getLifecycle().getCurrentState(), Lifecycle.State.RESUMED);
        assertEquals(testScene.getState(), State.RESUMED);
        assertTrue(testScene.isVisible());
        assertEquals(testScene.getView().getVisibility(), View.VISIBLE);
        assertTrue(testScene.getStateHistory().contains(State.RESUMED.getName()));

        sceneLifecycleManager.onPause();
        assertEquals(testScene.getLifecycle().getCurrentState(), Lifecycle.State.STARTED);
        assertEquals(testScene.getState(), State.STARTED);
        assertTrue(testScene.isVisible());
        assertEquals(testScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onStop();
        assertEquals(testScene.getLifecycle().getCurrentState(), Lifecycle.State.CREATED);
        assertEquals(testScene.getState(), State.ACTIVITY_CREATED);
        assertFalse(testScene.isVisible());
        assertEquals(testScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onDestroyView();
        assertNull(testScene.getView());
        assertNull(testScene.getActivity());
        assertNull(testScene.getApplicationContext());
        assertNull(testScene.getParentScene());
        assertEquals(testScene.getLifecycle().getCurrentState(), Lifecycle.State.DESTROYED);
        assertEquals(testScene.getState(), State.NONE);
        assertFalse(testScene.isVisible());
    }

    public static class TestScene extends Scene {

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }
}