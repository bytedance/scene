package com.bytedance.scene;


import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.navigation.ConfigurationChangedListener;
import com.bytedance.scene.navigation.NavigationListener;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.OnBackPressedListener;
import com.bytedance.scene.utlity.ViewIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NavigationSceneListenerTests {
    @Test
    public void testOnBackPressListener() {
        TestScene rootScene = new TestScene();
        Pair<SceneLifecycleManager, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;
        final NavigationScene navigationScene = pair.second;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        TestChildScene child = new TestChildScene();
        navigationScene.push(child);

        final boolean[] value = new boolean[1];
        navigationScene.addOnBackPressedListener(child, new OnBackPressedListener() {
            @Override
            public boolean onBackPressed() {
                navigationScene.removeOnBackPressedListener(this);
                value[0] = true;
                return true;
            }
        });

        assertTrue(navigationScene.onBackPressed());
        assertTrue(value[0]);
        assertTrue(navigationScene.onBackPressed());
        assertFalse(navigationScene.onBackPressed());
    }

    @Test
    public void testNavigationListener() {
        TestScene rootScene = new TestScene();
        Pair<SceneLifecycleManager, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        final TestChildScene child = new TestChildScene();
        navigationScene.push(child);

        NavigationListener navigationListener = new NavigationListener() {
            @Override
            public void navigationChange(@Nullable Scene from, @NonNull Scene to, boolean isPush) {
                assertSame(from, child);
                assertSame(to.getClass(), TestChildScene.class);
                assertTrue(isPush);
            }
        };
        navigationScene.addNavigationListener(child, navigationListener);
        navigationScene.push(TestChildScene.class);

        navigationScene.removeNavigationListener(navigationListener);
        navigationListener = new NavigationListener() {
            @Override
            public void navigationChange(@Nullable Scene from, @NonNull Scene to, boolean isPush) {
                assertSame(from.getClass(), TestChildScene.class);
                assertSame(to, child);
                assertFalse(isPush);
            }
        };
        navigationScene.addNavigationListener(child, navigationListener);
        navigationScene.pop();
    }

    @Test
    public void testNavigationListenerRemoveAfterLifecycleDestroy() {
        TestScene rootScene = new TestScene();
        Pair<SceneLifecycleManager, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        final TestChildScene child = new TestChildScene();
        navigationScene.push(child);

        final AtomicBoolean called = new AtomicBoolean(false);
        NavigationListener navigationListener = new NavigationListener() {
            @Override
            public void navigationChange(@Nullable Scene from, @NonNull Scene to, boolean isPush) {
                called.set(true);
            }
        };
        navigationScene.addNavigationListener(child, navigationListener);
        navigationScene.remove(child);

        navigationScene.push(TestChildScene.class);
        assertFalse(called.get());
    }

    @Test
    public void testNavigationListenerNotAddAfterLifecycleDestroy() {
        TestScene rootScene = new TestScene();
        Pair<SceneLifecycleManager, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        final TestChildScene child = new TestChildScene();
        navigationScene.push(child);
        navigationScene.remove(child);

        final AtomicBoolean called = new AtomicBoolean(false);
        NavigationListener navigationListener = new NavigationListener() {
            @Override
            public void navigationChange(@Nullable Scene from, @NonNull Scene to, boolean isPush) {
                called.set(true);
            }
        };
        navigationScene.addNavigationListener(child, navigationListener);

        navigationScene.push(TestChildScene.class);
        assertFalse(called.get());
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
