package com.bytedance.scene.navigation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.SceneGlobalConfig;
import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.Scope;
import com.bytedance.scene.State;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.utlity.ViewIdGenerator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NavigationSceneLifecycleSeparateTests {

    @Test
    public void test_disable_setSeparateCreateFromCreateView() {
        final TestScene rootScene = new TestScene();
        rootScene.setSeparateCreateFromCreateView(true);

        Assert.assertThrows(IllegalStateException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);
            }
        });
    }

    @Test
    public void test_setSeparateCreateFromCreateView() {
        final TestScene rootScene = new TestScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene, true);
        final NavigationScene navigationScene = pair.second;

        Assert.assertThrows(IllegalStateException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                navigationScene.setSeparateCreateFromCreateView(false);
            }
        });

        Assert.assertThrows(IllegalStateException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                rootScene.setSeparateCreateFromCreateView(false);
            }
        });

        final Scene secondScene = new TestChildScene();
        secondScene.setSeparateCreateFromCreateView(false);

        navigationScene.push(secondScene);
        Assert.assertTrue(secondScene.isSeparateCreateFromCreateView());
        Assert.assertThrows(IllegalStateException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                secondScene.setSeparateCreateFromCreateView(false);
            }
        });
    }

    /**
     * non translucent Scene +  non translucent Scene
     */
    @Test
    public void testNavigationSceneLifecycle() {
        final TestScene groupScene = new TestScene();
        TestChildScene secondScene = new TestChildScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene, true);
        assertNotNull(groupScene.getView().getParent());

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);//make sure not be affected by animation

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

        View secondSceneView = secondScene.getView();
        navigationScene.pop();
        assertEquals(secondScene.getState(), State.NONE);
        assertNull(secondSceneView.getParent());
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

        //start state
        sceneLifecycleManager.onStart();
        assertEquals(secondScene.getState(), State.STARTED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

        secondSceneView = secondScene.getView();
        navigationScene.pop();
        assertEquals(secondScene.getState(), State.NONE);
        assertNull(secondSceneView.getParent());
        assertEquals(groupScene.getState(), State.STARTED);

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.STARTED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

        //resume state
        sceneLifecycleManager.onResume();
        assertEquals(secondScene.getState(), State.RESUMED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

        secondSceneView = secondScene.getView();
        navigationScene.pop();
        assertEquals(secondScene.getState(), State.NONE);
        assertNull(secondSceneView.getParent());
        assertEquals(groupScene.getState(), State.RESUMED);

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.RESUMED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

        //pause state
        sceneLifecycleManager.onPause();
        assertEquals(secondScene.getState(), State.STARTED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

        secondSceneView = secondScene.getView();
        navigationScene.pop();
        assertEquals(secondScene.getState(), State.NONE);
        assertNull(secondSceneView.getParent());
        assertEquals(groupScene.getState(), State.STARTED);

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.STARTED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

        //stop state
        sceneLifecycleManager.onStop();
        assertEquals(secondScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

        secondSceneView = secondScene.getView();
        navigationScene.pop();
        assertEquals(secondScene.getState(), State.NONE);
        assertNull(secondSceneView.getParent());
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

        //destroy state
        secondSceneView = secondScene.getView();
        View groupSceneView = groupScene.getView();
        sceneLifecycleManager.onDestroyView();
        assertEquals(secondScene.getState(), State.NONE);
        assertNotNull(secondSceneView.getParent());
        assertEquals(groupScene.getState(), State.NONE);
        assertNotNull(groupSceneView.getParent());
    }

    /**
     * stop order, FILO
     */
    @Test
    public void testStopOrder() {
        final TestScene groupScene = new TestScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene, true);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        final StringBuilder closeLog = new StringBuilder();

        navigationScene.push(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onStop() {
                super.onStop();
                closeLog.append("0");
            }
        });

        navigationScene.push(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onStop() {
                super.onStop();
                closeLog.append("1");
            }
        },new PushOptions.Builder().build());

        navigationScene.push(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onStop() {
                super.onStop();
                closeLog.append("2");
            }
        },new PushOptions.Builder().build());

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();

        assertEquals("2", closeLog.toString());
    }

    /**
     * destroy order, FILO
     */
    @Test
    public void testDestroyOrder() {
        final TestScene groupScene = new TestScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene, true);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        final StringBuilder closeLog = new StringBuilder();

        navigationScene.push(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onDestroyView() {
                super.onDestroyView();
                closeLog.append("0a");
            }

            @Override
            public void onDestroy() {
                super.onDestroy();
                closeLog.append("0b");
            }
        });

        navigationScene.push(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onDestroyView() {
                super.onDestroyView();
                closeLog.append("1a");
            }

            @Override
            public void onDestroy() {
                super.onDestroy();
                closeLog.append("1b");
            }
        });

        navigationScene.push(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onDestroyView() {
                super.onDestroyView();
                closeLog.append("2a");
            }

            @Override
            public void onDestroy() {
                super.onDestroy();
                closeLog.append("2b");
            }
        });

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onDestroyView();

        assertEquals("2a1a0a2b1b0b", closeLog.toString());
    }

    @Test
    public void testNavigationSceneLifecycle_separateCreateCall() {
        final TestScene rootScene = new TestScene();

        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();
        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(rootScene.getClass());
        options.setOnlyRestoreVisibleScene(true);
        navigationScene.setSeparateCreateFromCreateView(true);
        navigationScene.setInitRootSceneOnCreate(false);
        navigationScene.setArguments(options.toBundle());
        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };
        navigationScene.setRootScopeFactory(rootScopeFactory);
        SceneComponentFactory sceneComponentFactory = new SceneComponentFactory() {
            @Override
            public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                if (className.equals(rootScene.getClass().getName())) {
                    return rootScene;
                }
                return null;
            }
        };
        navigationScene.setRootSceneComponentFactory(sceneComponentFactory);
        navigationScene.setDefaultNavigationAnimationExecutor(null);//make sure not be affected by animation

        navigationScene.dispatchAttachActivity(testActivity);
        navigationScene.dispatchAttachScene(null);
        navigationScene.dispatchCreate(null);
        assertEquals(State.CREATED, navigationScene.getState());
        assertEquals(State.NONE, rootScene.getState());

        TestChildScene secondScene = new TestChildScene();
        assertEquals(secondScene.getState(), State.NONE);
        assertEquals(rootScene.getState(), State.NONE);

        navigationScene.dispatchCreateView(null, testActivity.mFrameLayout);
        assertNull(rootScene.getView());
        assertEquals(State.NONE, rootScene.getState());

        navigationScene.dispatchActivityCreated(null);

        assertTrue(rootScene.isSeparateCreateFromCreateView());

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);
        assertNotNull(rootScene.getView());

        assertTrue(secondScene.isSeparateCreateFromCreateView());

        View secondSceneView = secondScene.getView();
        navigationScene.pop();
        assertEquals(secondScene.getState(), State.NONE);
        assertNull(secondSceneView.getParent());
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        //start state
        navigationScene.dispatchStart();
        assertEquals(secondScene.getState(), State.STARTED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        secondSceneView = secondScene.getView();
        navigationScene.pop();
        assertEquals(secondScene.getState(), State.NONE);
        assertNull(secondSceneView.getParent());
        assertEquals(rootScene.getState(), State.STARTED);

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.STARTED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        //resume state
        navigationScene.dispatchResume();
        assertEquals(secondScene.getState(), State.RESUMED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        secondSceneView = secondScene.getView();
        navigationScene.pop();
        assertEquals(secondScene.getState(), State.NONE);
        assertNull(secondSceneView.getParent());
        assertEquals(rootScene.getState(), State.RESUMED);

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.RESUMED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        //pause state
        navigationScene.dispatchPause();
        assertEquals(secondScene.getState(), State.STARTED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        secondSceneView = secondScene.getView();
        navigationScene.pop();
        assertEquals(secondScene.getState(), State.NONE);
        assertNull(secondSceneView.getParent());
        assertEquals(rootScene.getState(), State.STARTED);

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.STARTED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        //stop state
        navigationScene.dispatchStop();
        assertEquals(secondScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        secondSceneView = secondScene.getView();
        navigationScene.pop();
        assertEquals(secondScene.getState(), State.NONE);
        assertNull(secondSceneView.getParent());
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        //destroy state
        secondSceneView = secondScene.getView();
        View groupSceneView = rootScene.getView();
        navigationScene.dispatchDestroyView();
        assertEquals(secondScene.getState(), State.CREATED);
        assertNotNull(secondSceneView.getParent());
        assertEquals(rootScene.getState(), State.CREATED);
        assertNotNull(groupSceneView.getParent());

        navigationScene.dispatchDestroy();
        navigationScene.dispatchDetachScene();
        navigationScene.dispatchDetachActivity();
        assertEquals(secondScene.getState(), State.NONE);
        assertNotNull(secondSceneView.getParent());
        assertEquals(rootScene.getState(), State.NONE);
        assertNotNull(groupSceneView.getParent());
    }

    @Test
    public void testNavigationSceneLifecycle_separateCreateCall_initRootSceneOnCreate() {
        final TestScene rootScene = new TestScene();

        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();
        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(rootScene.getClass());
        options.setOnlyRestoreVisibleScene(true);
        navigationScene.setSeparateCreateFromCreateView(true);
        navigationScene.setInitRootSceneOnCreate(true);
        navigationScene.setArguments(options.toBundle());
        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };
        navigationScene.setRootScopeFactory(rootScopeFactory);
        SceneComponentFactory sceneComponentFactory = new SceneComponentFactory() {
            @Override
            public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                if (className.equals(rootScene.getClass().getName())) {
                    return rootScene;
                }
                return null;
            }
        };
        navigationScene.setRootSceneComponentFactory(sceneComponentFactory);
        navigationScene.setDefaultNavigationAnimationExecutor(null);//make sure not be affected by animation

        navigationScene.dispatchAttachActivity(testActivity);
        navigationScene.dispatchAttachScene(null);
        navigationScene.dispatchCreate(null);
        assertEquals(State.CREATED, navigationScene.getState());
        assertEquals(State.CREATED, rootScene.getState());
        assertNull(rootScene.getView());

        TestChildScene secondScene = new TestChildScene();
        assertEquals(secondScene.getState(), State.NONE);

        navigationScene.dispatchCreateView(null, testActivity.mFrameLayout);
        assertEquals(State.VIEW_CREATED, rootScene.getState());
        assertNotNull(rootScene.getView());
        assertTrue(rootScene.isSeparateCreateFromCreateView());

        navigationScene.dispatchActivityCreated(null);
        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        assertTrue(secondScene.isSeparateCreateFromCreateView());


        View secondSceneView = secondScene.getView();
        navigationScene.pop();
        assertEquals(secondScene.getState(), State.NONE);
        assertNull(secondSceneView.getParent());
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        //start state
        navigationScene.dispatchStart();
        assertEquals(secondScene.getState(), State.STARTED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        secondSceneView = secondScene.getView();
        navigationScene.pop();
        assertEquals(secondScene.getState(), State.NONE);
        assertNull(secondSceneView.getParent());
        assertEquals(rootScene.getState(), State.STARTED);

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.STARTED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        //resume state
        navigationScene.dispatchResume();
        assertEquals(secondScene.getState(), State.RESUMED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        secondSceneView = secondScene.getView();
        navigationScene.pop();
        assertEquals(secondScene.getState(), State.NONE);
        assertNull(secondSceneView.getParent());
        assertEquals(rootScene.getState(), State.RESUMED);

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.RESUMED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        //pause state
        navigationScene.dispatchPause();
        assertEquals(secondScene.getState(), State.STARTED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        secondSceneView = secondScene.getView();
        navigationScene.pop();
        assertEquals(secondScene.getState(), State.NONE);
        assertNull(secondSceneView.getParent());
        assertEquals(rootScene.getState(), State.STARTED);

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.STARTED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        //stop state
        navigationScene.dispatchStop();
        assertEquals(secondScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        secondSceneView = secondScene.getView();
        navigationScene.pop();
        assertEquals(secondScene.getState(), State.NONE);
        assertNull(secondSceneView.getParent());
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        //destroy state
        secondSceneView = secondScene.getView();
        View groupSceneView = rootScene.getView();
        navigationScene.dispatchDestroyView();
        assertEquals(secondScene.getState(), State.CREATED);
        assertNotNull(secondSceneView.getParent());
        assertEquals(rootScene.getState(), State.CREATED);
        assertNotNull(groupSceneView.getParent());

        navigationScene.dispatchDestroy();
        navigationScene.dispatchDetachScene();
        navigationScene.dispatchDetachActivity();
        assertEquals(secondScene.getState(), State.NONE);
        assertNotNull(secondSceneView.getParent());
        assertEquals(rootScene.getState(), State.NONE);
        assertNotNull(groupSceneView.getParent());
    }

    @Test
    public void testNavigationScene_separateCreateCall_initRootSceneOnCreate_onlyRestoreVisibleScene() {
        final TestScene rootScene = new TestScene();
        rootScene.customId = 1;

        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();
        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(rootScene.getClass());
        options.setOnlyRestoreVisibleScene(true);
        navigationScene.setSeparateCreateFromCreateView(true);
        navigationScene.setInitRootSceneOnCreate(true);
        navigationScene.setArguments(options.toBundle());
        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };
        navigationScene.setRootScopeFactory(rootScopeFactory);
        SceneComponentFactory sceneComponentFactory = new SceneComponentFactory() {
            @Override
            public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                if (className.equals(rootScene.getClass().getName())) {
                    return rootScene;
                }
                return null;
            }
        };
        navigationScene.setRootSceneComponentFactory(sceneComponentFactory);
        navigationScene.setDefaultNavigationAnimationExecutor(null);//make sure not be affected by animation

        navigationScene.dispatchAttachActivity(testActivity);
        navigationScene.dispatchAttachScene(null);
        navigationScene.dispatchCreate(null);
        navigationScene.dispatchCreateView(null, testActivity.mFrameLayout);
        navigationScene.dispatchActivityCreated(null);
        navigationScene.dispatchStart();
        navigationScene.dispatchResume();

        TestChildScene secondScene = new TestChildScene();
        secondScene.customId = 2;
        assertEquals(secondScene.getState(), State.NONE);

        navigationScene.push(secondScene);
        assertEquals(State.RESUMED, secondScene.getState());

        Bundle bundle = new Bundle();
        navigationScene.dispatchPause();
        navigationScene.dispatchStop();
        navigationScene.onSaveInstanceState(bundle);
        navigationScene.dispatchDestroyView();
        navigationScene.dispatchDestroy();
        navigationScene.dispatchDetachScene();
        navigationScene.dispatchDetachActivity();

        /// start restore
        NavigationScene navigationScene2 = new NavigationScene();
        navigationScene2.setSeparateCreateFromCreateView(true);
        navigationScene2.setInitRootSceneOnCreate(true);
        navigationScene2.setArguments(options.toBundle());
        navigationScene2.setRootScopeFactory(rootScopeFactory);
        navigationScene2.setRootSceneComponentFactory(sceneComponentFactory);
        navigationScene2.setDefaultNavigationAnimationExecutor(null);

        navigationScene2.dispatchAttachActivity(testActivity);
        navigationScene2.dispatchAttachScene(null);
        navigationScene2.dispatchCreate(bundle);

        assertEquals(2, navigationScene2.getSceneList().size());
        Scene topScene = navigationScene2.getCurrentScene();
        Scene restoreRootScene = navigationScene2.getSceneList().get(0);
        assertTrue(topScene instanceof TestChildScene);
        assertEquals(2, ((TestChildScene) topScene).customId);
        assertTrue(restoreRootScene instanceof TestScene);
        assertEquals(1, ((TestScene) restoreRootScene).customId);
        assertEquals(State.CREATED, topScene.getState());
        assertEquals(State.NONE, restoreRootScene.getState());

        navigationScene2.dispatchCreateView(bundle, testActivity.mFrameLayout);
        navigationScene2.dispatchActivityCreated(bundle);
        navigationScene2.dispatchStart();
        navigationScene2.dispatchResume();
        assertEquals(State.RESUMED, topScene.getState());
        assertEquals(State.NONE, restoreRootScene.getState());

        navigationScene2.pop();
        assertEquals(State.NONE, topScene.getState());
        assertEquals(State.RESUMED, restoreRootScene.getState());
    }

    public static class TestScene extends GroupScene {
        public final int mId;

        public int customId = -1;

        public TestScene() {
            mId = ViewIdGenerator.generateViewId();
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
                customId = savedInstanceState.getInt("customId", -1);
            }
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

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            assertTrue(View.VISIBLE == getView().getVisibility() || View.GONE == getView().getVisibility());
        }

        @Override
        public void onStart() {
            super.onStart();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onResume() {
            super.onResume();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onPause() {
            super.onPause();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onStop() {
            super.onStop();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("customId", customId);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            assertTrue(View.VISIBLE == getView().getVisibility() || View.GONE == getView().getVisibility());
        }
    }


    public static class TestChildScene extends Scene {

        public int customId = -1;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
                customId = savedInstanceState.getInt("customId", -1);
            }
        }

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            assertTrue(View.VISIBLE == getView().getVisibility() || View.GONE == getView().getVisibility());
        }

        @Override
        public void onStart() {
            super.onStart();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onResume() {
            super.onResume();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onPause() {
            super.onPause();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onStop() {
            super.onStop();
            assertEquals(View.VISIBLE, getView().getVisibility());
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("customId", customId);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            assertTrue(View.VISIBLE == getView().getVisibility() || View.GONE == getView().getVisibility());
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

