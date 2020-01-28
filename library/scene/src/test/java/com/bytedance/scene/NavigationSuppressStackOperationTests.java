package com.bytedance.scene;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bytedance.scene.navigation.NavigationScene;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.util.concurrent.atomic.AtomicBoolean;

import static android.os.Looper.getMainLooper;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

/**
 * invoke navigation operation in lifecycle callbacks
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
@LooperMode(PAUSED)
public class NavigationSuppressStackOperationTests {
    @Test
    public void test_Immediate_Push_Pop_Remove() {
        Scene rootScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(rootScene);
        Scene second = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        navigationScene.push(second);
        assertEquals(second, navigationScene.getCurrentScene());
        navigationScene.remove(second);
        assertEquals(rootScene, navigationScene.getCurrentScene());
        navigationScene.push(second);
        assertEquals(second, navigationScene.getCurrentScene());
        navigationScene.pop();
        assertEquals(rootScene, navigationScene.getCurrentScene());
    }

    @Test
    public void test_Enter_Post_Push() {
        final AtomicBoolean called = new AtomicBoolean(false);
        Scene rootScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);

                Scene second = new Scene() {
                    @NonNull
                    @Override
                    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                        return new View(requireSceneContext());
                    }
                };
                requireNavigationScene().push(second);
                assertEquals(this, requireNavigationScene().getCurrentScene());
                assertNotEquals(second, requireNavigationScene().getCurrentScene());
                called.set(true);
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(rootScene);
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertNotEquals(rootScene, rootScene.requireNavigationScene().getCurrentScene());
        assertTrue(called.get());
    }

    @Test
    public void test_Enter_Post_Pop() {
        final AtomicBoolean called = new AtomicBoolean(false);
        Scene rootScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(rootScene);
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                requireNavigationScene().pop();
                assertEquals(this, requireNavigationScene().getCurrentScene());
                called.set(true);
            }
        };
        navigationScene.push(scene);
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertEquals(State.NONE, scene.getState());
        assertTrue(called.get());
    }

    @Test
    public void test_Enter_Post_Remove() {
        final AtomicBoolean called = new AtomicBoolean(false);
        Scene rootScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(rootScene);
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                requireNavigationScene().remove(this);
                assertEquals(this, requireNavigationScene().getCurrentScene());
                called.set(true);
            }
        };
        navigationScene.push(scene);
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertEquals(State.NONE, scene.getState());
        assertTrue(called.get());
    }

    @Test
    public void test_Exit_Post_Push() {
        final AtomicBoolean called = new AtomicBoolean(false);
        Scene rootScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onPause() {
                super.onPause();
                Scene second = new Scene() {
                    @NonNull
                    @Override
                    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                        return new View(requireSceneContext());
                    }
                };
                requireNavigationScene().push(second);
                assertEquals(this, requireNavigationScene().getCurrentScene());
                assertNotEquals(second, requireNavigationScene().getCurrentScene());
                called.set(true);
            }
        };
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);
        SceneLifecycleManager<NavigationScene> lifecycleManager = pair.first;

        lifecycleManager.onStart();
        lifecycleManager.onResume();
        lifecycleManager.onPause();
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertNotEquals(rootScene, rootScene.requireNavigationScene().getCurrentScene());
        assertTrue(called.get());
    }

    @Test
    public void test_Exit_Post_Pop() {
        final AtomicBoolean called = new AtomicBoolean(false);
        Scene rootScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);
        SceneLifecycleManager<NavigationScene> lifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        lifecycleManager.onStart();
        lifecycleManager.onResume();

        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onPause() {
                super.onPause();
                requireNavigationScene().pop();
                assertEquals(this, requireNavigationScene().getCurrentScene());
                called.set(true);
            }
        };
        navigationScene.push(scene);

        lifecycleManager.onPause();
        shadowOf(getMainLooper()).idle();//execute Handler posted task\
        assertEquals(State.NONE, scene.getState());
        assertTrue(called.get());
    }

    @Test
    public void test_Exit_Post_Remove() {
        final AtomicBoolean called = new AtomicBoolean(false);
        Scene rootScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);
        SceneLifecycleManager<NavigationScene> lifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        lifecycleManager.onStart();
        lifecycleManager.onResume();

        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onPause() {
                super.onPause();
                requireNavigationScene().remove(this);
                assertEquals(this, requireNavigationScene().getCurrentScene());
                called.set(true);
            }
        };
        navigationScene.push(scene);

        lifecycleManager.onPause();
        shadowOf(getMainLooper()).idle();//execute Handler posted task\
        assertEquals(State.NONE, scene.getState());
        assertTrue(called.get());
    }
}
