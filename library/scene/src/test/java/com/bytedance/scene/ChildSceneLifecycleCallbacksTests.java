package com.bytedance.scene;


import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.core.util.Pair;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.ChildSceneLifecycleAdapterCallbacks;
import com.bytedance.scene.interfaces.ChildSceneLifecycleCallbacks;
import com.bytedance.scene.utlity.ViewIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ChildSceneLifecycleCallbacksTests {
    @Test
    public void testGroupSceneNotRecursiveExcludingOnSceneSaveInstanceState() {
        final AtomicBoolean isCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isViewCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isActivityCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isStartedCalled = new AtomicBoolean(false);
        final AtomicBoolean isResumedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPausedCalled = new AtomicBoolean(false);
        final AtomicBoolean isStoppedCalled = new AtomicBoolean(false);
        final AtomicBoolean isViewDestroyedCalled = new AtomicBoolean(false);
        final AtomicBoolean isDestroyedCalled = new AtomicBoolean(false);

        ChildSceneLifecycleCallbacks callbacks = new ChildSceneLifecycleCallbacks() {
            @Override
            public void onPreSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onPreSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onPreSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onPreSceneStarted(@NonNull Scene scene) {

            }

            @Override
            public void onPreSceneResumed(@NonNull Scene scene) {

            }

            @Override
            public void onPreScenePaused(@NonNull Scene scene) {

            }

            @Override
            public void onPreSceneStopped(@NonNull Scene scene) {

            }

            @Override
            public void onPreSceneViewDestroyed(@NonNull Scene scene) {

            }

            @Override
            public void onPreSceneDestroyed(@NonNull Scene scene) {

            }

            @Override
            public void onSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                if (!isCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNull(scene.getView());
                assertSame(State.NONE, scene.getState());
            }

            @Override
            public void onSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                if (!isViewCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNotNull(scene.getView());
                assertSame(State.NONE, scene.getState());
            }

            @Override
            public void onSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                if (!isActivityCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNotNull(scene.getView());
                assertSame(State.VIEW_CREATED, scene.getState());//Scene state is set to State.Activity_CREATED after onActivityCreated() and onSceneActivityCreated()
            }

            @Override
            public void onSceneStarted(@NonNull Scene scene) {
                if (!isStartedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertSame(State.ACTIVITY_CREATED, scene.getState());
            }

            @Override
            public void onSceneResumed(@NonNull Scene scene) {
                if (!isResumedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertSame(State.STARTED, scene.getState());
            }

            @Override
            public void onSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState) {
                //ignored
            }

            @Override
            public void onScenePaused(@NonNull Scene scene) {
                if (!isPausedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertSame(State.STARTED, scene.getState());
            }

            @Override
            public void onSceneStopped(@NonNull Scene scene) {
                if (!isStoppedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertSame(State.ACTIVITY_CREATED, scene.getState());
            }

            @Override
            public void onSceneViewDestroyed(@NonNull Scene scene) {
                if (!isViewDestroyedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNotNull(scene.getView());
                assertSame(State.NONE, scene.getState());
            }

            @Override
            public void onSceneDestroyed(@NonNull Scene scene) {
                if (!isDestroyedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNull(scene.getView());
            }
        };

        final int id = ViewIdGenerator.generateViewId();
        final GroupScene rootScene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                getView().setId(id);
            }
        };

        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);
        rootScene.registerChildSceneLifecycleCallbacks(callbacks, false);

        SceneLifecycleManager lifecycleManager = pair.first;
        lifecycleManager.onStart();
        lifecycleManager.onResume();

        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };

        rootScene.add(id, scene, "TAG");
        assertTrue(isCreatedCalled.get());
        assertTrue(isViewCreatedCalled.get());
        assertTrue(isActivityCreatedCalled.get());
        assertTrue(isStartedCalled.get());
        assertTrue(isResumedCalled.get());

        rootScene.remove(scene);
        assertTrue(isPausedCalled.get());
        assertTrue(isStoppedCalled.get());
        assertTrue(isViewDestroyedCalled.get());
        assertTrue(isDestroyedCalled.get());
    }

    @Test
    public void testGroupSceneRecursiveExcludingOnSceneSaveInstanceState() {
        final AtomicBoolean isPreCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPreViewCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPreActivityCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPreStartedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPreResumedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPrePausedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPreStoppedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPreViewDestroyedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPreDestroyedCalled = new AtomicBoolean(false);

        final AtomicBoolean isCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isViewCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isActivityCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean isStartedCalled = new AtomicBoolean(false);
        final AtomicBoolean isResumedCalled = new AtomicBoolean(false);
        final AtomicBoolean isPausedCalled = new AtomicBoolean(false);
        final AtomicBoolean isStoppedCalled = new AtomicBoolean(false);
        final AtomicBoolean isViewDestroyedCalled = new AtomicBoolean(false);
        final AtomicBoolean isDestroyedCalled = new AtomicBoolean(false);

        final Scene childScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };

        ChildSceneLifecycleCallbacks callbacks = new ChildSceneLifecycleCallbacks() {
            @Override
            public void onPreSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                if (scene != childScene) {
                    return;
                }
                if (!isPreCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNull(scene.getView());
            }

            @Override
            public void onPreSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                if (scene != childScene) {
                    return;
                }
                if (!isPreViewCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNotNull(scene.getView());
            }

            @Override
            public void onPreSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                if (scene != childScene) {
                    return;
                }
                if (!isPreActivityCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNotNull(scene.getView());
            }

            @Override
            public void onPreSceneStarted(@NonNull Scene scene) {
                if (scene != childScene) {
                    return;
                }
                if (!isPreStartedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onPreSceneResumed(@NonNull Scene scene) {
                if (scene != childScene) {
                    return;
                }
                if (!isPreResumedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onPreScenePaused(@NonNull Scene scene) {
                if (scene != childScene) {
                    return;
                }
                if (!isPrePausedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onPreSceneStopped(@NonNull Scene scene) {
                if (scene != childScene) {
                    return;
                }
                if (!isPreStoppedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onPreSceneViewDestroyed(@NonNull Scene scene) {
                if (scene != childScene) {
                    return;
                }
                if (!isPreViewDestroyedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onPreSceneDestroyed(@NonNull Scene scene) {
                if (scene != childScene) {
                    return;
                }
                if (!isPreDestroyedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                if (scene != childScene) {
                    return;
                }
                if (!isCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNull(scene.getView());
                assertSame(State.NONE, scene.getState());
            }

            @Override
            public void onSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                if (scene != childScene) {
                    return;
                }
                if (!isViewCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNotNull(scene.getView());
                assertSame(State.NONE, scene.getState());
            }

            @Override
            public void onSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
                if (scene != childScene) {
                    return;
                }
                if (!isActivityCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNotNull(scene.getView());
                assertSame(State.VIEW_CREATED, scene.getState());//Scene state is set to State.Activity_CREATED after onActivityCreated() and onSceneActivityCreated()
            }

            @Override
            public void onSceneStarted(@NonNull Scene scene) {
                if (scene != childScene) {
                    return;
                }
                if (!isStartedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertSame(State.ACTIVITY_CREATED, scene.getState());
            }

            @Override
            public void onSceneResumed(@NonNull Scene scene) {
                if (scene != childScene) {
                    return;
                }
                if (!isResumedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertSame(State.STARTED, scene.getState());
            }

            @Override
            public void onSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState) {
                //ignored
            }

            @Override
            public void onScenePaused(@NonNull Scene scene) {
                if (scene != childScene) {
                    return;
                }
                if (!isPausedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertSame(State.STARTED, scene.getState());
            }

            @Override
            public void onSceneStopped(@NonNull Scene scene) {
                if (scene != childScene) {
                    return;
                }
                if (!isStoppedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertSame(State.ACTIVITY_CREATED, scene.getState());
            }

            @Override
            public void onSceneViewDestroyed(@NonNull Scene scene) {
                if (scene != childScene) {
                    return;
                }
                if (!isViewDestroyedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNotNull(scene.getView());
                assertSame(State.NONE, scene.getState());
            }

            @Override
            public void onSceneDestroyed(@NonNull Scene scene) {
                if (scene != childScene) {
                    return;
                }
                if (!isDestroyedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
                assertNull(scene.getView());
            }
        };

        final int rootId = ViewIdGenerator.generateViewId();
        final GroupScene rootScene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                getView().setId(rootId);
            }
        };

        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);
        rootScene.registerChildSceneLifecycleCallbacks(callbacks, true);

        SceneLifecycleManager lifecycleManager = pair.first;
        lifecycleManager.onStart();
        lifecycleManager.onResume();

        final int secondRootId = ViewIdGenerator.generateViewId();
        final GroupScene secondRootScene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                getView().setId(secondRootId);
            }
        };
        rootScene.add(rootId, secondRootScene, "TAG");

        secondRootScene.add(secondRootId, childScene, "CHILD");
        assertTrue(isCreatedCalled.get());
        assertTrue(isViewCreatedCalled.get());
        assertTrue(isActivityCreatedCalled.get());
        assertTrue(isStartedCalled.get());
        assertTrue(isResumedCalled.get());

        secondRootScene.remove(childScene);
        assertTrue(isPausedCalled.get());
        assertTrue(isStoppedCalled.get());
        assertTrue(isViewDestroyedCalled.get());
        assertTrue(isDestroyedCalled.get());

        assertTrue(isPreCreatedCalled.get());
        assertTrue(isPreViewCreatedCalled.get());
        assertTrue(isPreActivityCreatedCalled.get());
        assertTrue(isPreStartedCalled.get());
        assertTrue(isPreResumedCalled.get());
        assertTrue(isPrePausedCalled.get());
        assertTrue(isPreStoppedCalled.get());
        assertTrue(isPreViewDestroyedCalled.get());
        assertTrue(isPreDestroyedCalled.get());
    }

    @Test
    public void testGroupSceneUnregisterChildSceneLifecycleCallbacks() {
        ChildSceneLifecycleCallbacks callbacks = new ChildSceneLifecycleAdapterCallbacks();
        GroupScene groupScene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }
        };
        groupScene.registerChildSceneLifecycleCallbacks(callbacks, false);
        groupScene.unregisterChildSceneLifecycleCallbacks(callbacks);
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
