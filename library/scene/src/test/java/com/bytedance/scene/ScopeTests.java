package com.bytedance.scene;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.ViewIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ScopeTests {
    @Test
    public void testRegisterScope() {
        TestScene rootScene = new TestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        navigationScene.getScope().register("navigation_key", "navigation_value");
        assertEquals(navigationScene.getScope().getService("navigation_key"), "navigation_value");
        assertEquals(rootScene.getScope().getService("navigation_key"), "navigation_value");

        TestChildScene childScene = new TestChildScene();
        rootScene.add(rootScene.mId, childScene, "childScene");

        rootScene.getScope().register("parent_key", "parent_value");
        assertEquals(childScene.getScope().getService("parent_key"), "parent_value");
        assertEquals(childScene.getScope().getService("navigation_key"), "navigation_value");

        childScene.getScope().register("child_key", "child_value");
        assertEquals(childScene.getScope().getService("child_key"), "child_value");
        assertNull(rootScene.getScope().getService("child_key"));
        assertNull(navigationScene.getScope().getService("child_key"));
    }

    @Test
    public void testUnRegisterScope() {
        TestScene rootScene = new TestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        final boolean[] value = new boolean[1];
        navigationScene.getScope().register("navigation_key", new Scope.Scoped() {
            @Override
            public void onUnRegister() {
                value[0] = true;
            }
        });
        assertNotNull(navigationScene.getScope().getService("navigation_key"));
        navigationScene.getScope().unRegister("navigation_key");
        assertNull(navigationScene.getScope().getService("child_key"));
        assertTrue(value[0]);
    }

    @Test
    public void testUnRegisterScopeByDestroy() {
        TestScene rootScene = new TestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        final boolean[] value = new boolean[1];
        navigationScene.getScope().register("navigation_key", new Scope.Scoped() {
            @Override
            public void onUnRegister() {
                value[0] = true;
            }
        });

        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onDestroyView();
        assertTrue(value[0]);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionsThrownWhenGetScopeBeforeSceneOnAttach() {
        TestScene rootScene = new TestScene();
        rootScene.getScope();
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

    public static class TestChildScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }
}
