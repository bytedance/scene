package com.bytedance.scene;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.ViewIdGenerator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GroupSceneLifecycleTests {
    @Test
    public void testAdd() {
        TestEmptyScene testScene = new TestEmptyScene();
        Pair<SceneLifecycleManager, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(testScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        TestChildScene childScene = new TestChildScene();
        testScene.add(testScene.mId, childScene, "childScene");

        assertTrue(testScene.isAdded(childScene));
        assertTrue(testScene.isShow(childScene));
        assertSame(childScene, testScene.findSceneByTag("childScene"));
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(1, testScene.getSceneList().size());
        assertSame(childScene, testScene.getSceneList().get(0));
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onStart();
        assertEquals(childScene.getState(), State.STARTED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onResume();
        assertEquals(childScene.getState(), State.RESUMED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onPause();
        assertEquals(childScene.getState(), State.STARTED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onStop();
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onStart();
        assertEquals(childScene.getState(), State.STARTED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onResume();
        assertEquals(childScene.getState(), State.RESUMED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onPause();
        assertEquals(childScene.getState(), State.STARTED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onStop();
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onDestroyView();
        assertEquals(childScene.getState(), State.NONE);
        assertNull(childScene.getView());
    }

    @Test
    public void testRemove() {
        TestEmptyScene testScene = new TestEmptyScene();
        Pair<SceneLifecycleManager, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(testScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        TestChildScene childScene = new TestChildScene();
        testScene.add(testScene.mId, childScene, "childScene");
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);

        testScene.remove(childScene);
        assertEquals(childScene.getState(), State.NONE);

        sceneLifecycleManager.onStart();
        testScene.add(testScene.mId, childScene, "childScene");
        assertEquals(childScene.getState(), State.STARTED);

        testScene.remove(childScene);
        assertEquals(childScene.getState(), State.NONE);

        sceneLifecycleManager.onResume();
        testScene.add(testScene.mId, childScene, "childScene");
        assertEquals(childScene.getState(), State.RESUMED);

        testScene.remove(childScene);
        assertEquals(childScene.getState(), State.NONE);
    }

    @Test
    public void testShowAndHide() {
        TestEmptyScene testScene = new TestEmptyScene();
        Pair<SceneLifecycleManager, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(testScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        TestChildScene childScene = new TestChildScene();
        assertEquals(childScene.getState(), State.NONE);

        testScene.add(testScene.mId, childScene, "childScene");
        assertTrue(testScene.isShow(childScene));
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertNotNull(childScene.getView());
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onStart();
        assertEquals(childScene.getState(), State.STARTED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        testScene.hide(childScene);
        assertFalse(testScene.isShow(childScene));
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getView().getVisibility(), View.GONE);

        testScene.show(childScene);
        assertTrue(testScene.isShow(childScene));
        assertEquals(childScene.getState(), State.STARTED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onResume();
        assertEquals(childScene.getState(), State.RESUMED);

        testScene.hide(childScene);
        assertFalse(testScene.isShow(childScene));
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getView().getVisibility(), View.GONE);

        testScene.show(childScene);
        assertTrue(testScene.isShow(childScene));
        assertEquals(childScene.getState(), State.RESUMED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onPause();
        assertEquals(childScene.getState(), State.STARTED);

        testScene.hide(childScene);
        assertFalse(testScene.isShow(childScene));
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getView().getVisibility(), View.GONE);

        testScene.show(childScene);
        assertTrue(testScene.isShow(childScene));
        assertEquals(childScene.getState(), State.STARTED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onStop();
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);

        testScene.hide(childScene);
        assertFalse(testScene.isShow(childScene));
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getView().getVisibility(), View.GONE);

        testScene.show(childScene);
        assertTrue(testScene.isShow(childScene));
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        sceneLifecycleManager.onDestroyView();
        assertNull(childScene.getView());
        assertEquals(childScene.getState(), State.NONE);

        testScene.hide(childScene);
        assertFalse(testScene.isShow(childScene));
        assertEquals(childScene.getState(), State.NONE);

        testScene.show(childScene);
        assertTrue(testScene.isShow(childScene));
        assertEquals(childScene.getState(), State.NONE);
    }

    @Test
    public void testShowAndHide2() {
        TestEmptyScene testScene = new TestEmptyScene();
        Pair<SceneLifecycleManager, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(testScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        TestChildScene childScene = new TestChildScene();
        assertEquals(childScene.getState(), State.NONE);

        testScene.add(testScene.mId, childScene, "childScene");
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        testScene.hide(childScene);
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getView().getVisibility(), View.GONE);

        sceneLifecycleManager.onStart();
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getView().getVisibility(), View.GONE);

        testScene.show(childScene);
        assertEquals(childScene.getState(), State.STARTED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        testScene.hide(childScene);
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getView().getVisibility(), View.GONE);

        sceneLifecycleManager.onResume();
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getView().getVisibility(), View.GONE);

        testScene.show(childScene);
        assertEquals(childScene.getState(), State.RESUMED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);

        testScene.hide(childScene);
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getView().getVisibility(), View.GONE);

        sceneLifecycleManager.onPause();
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getView().getVisibility(), View.GONE);

        testScene.show(childScene);
        assertEquals(childScene.getState(), State.STARTED);
        assertEquals(childScene.getView().getVisibility(), View.VISIBLE);
    }

    @Test
    public void testGroupSceneLifecycle() {
        TestScene groupScene = new TestScene();
        Pair<SceneLifecycleManager, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        TestChildScene childScene = new TestChildScene();

        //resume state
        groupScene.add(groupScene.mId, childScene, "childScene");

        assertTrue(groupScene.isAdded(childScene));
        assertEquals(childScene, groupScene.findSceneByTag("childScene"));
        assertEquals(childScene.getParentScene(), groupScene);
        assertEquals(childScene.getNavigationScene(), navigationScene);
        assertEquals(childScene.getState(), State.RESUMED);
        assertEquals(childScene.getLifecycle().getCurrentState(), groupScene.getLifecycle().getCurrentState());

        groupScene.hide(childScene);
        assertFalse(groupScene.isShow(childScene));
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getLifecycle().getCurrentState(), Lifecycle.State.CREATED);

        groupScene.show(childScene);
        assertTrue(groupScene.isShow(childScene));
        assertEquals(childScene.getState(), State.RESUMED);
        assertEquals(childScene.getLifecycle().getCurrentState(), Lifecycle.State.RESUMED);

        groupScene.remove(childScene);
        assertNull(groupScene.findSceneByTag("childScene"));
        assertFalse(groupScene.isAdded(childScene));
        assertNull(childScene.getView());
        assertNull(childScene.getActivity());
        assertEquals(childScene.getLifecycle().getCurrentState(), Lifecycle.State.DESTROYED);
        assertEquals(childScene.getState(), State.NONE);

        //pause state
        sceneLifecycleManager.onPause();

        groupScene.add(groupScene.mId, childScene, "childScene");
        assertEquals(childScene.getState(), State.STARTED);
        assertEquals(childScene.getLifecycle().getCurrentState(), Lifecycle.State.STARTED);

        groupScene.hide(childScene);
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getLifecycle().getCurrentState(), Lifecycle.State.CREATED);

        groupScene.show(childScene);
        assertEquals(childScene.getState(), State.STARTED);
        assertEquals(childScene.getLifecycle().getCurrentState(), Lifecycle.State.STARTED);

        groupScene.remove(childScene);

        //stop state
        sceneLifecycleManager.onStop();

        groupScene.add(groupScene.mId, childScene, "childScene");
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getLifecycle().getCurrentState(), Lifecycle.State.CREATED);

        groupScene.hide(childScene);
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getLifecycle().getCurrentState(), Lifecycle.State.CREATED);

        groupScene.show(childScene);
        assertEquals(childScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(childScene.getLifecycle().getCurrentState(), Lifecycle.State.CREATED);
    }

    public static class TestEmptyScene extends GroupScene {
        public final int mId;

        public TestEmptyScene() {
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
        public void onDestroyView() {
            super.onDestroyView();
            assertTrue(View.VISIBLE == getView().getVisibility() || View.GONE == getView().getVisibility());
        }
    }

    public static class TestScene extends GroupScene {
        private static final String CHILD_SCENE_CONSTRUCTOR = "constructor";
        private static final String CHILD_SCENE_ATTACH = "attach";
        private static final String CHILD_SCENE_CREATE_VIEW = "CreateView";
        private static final String CHILD_SCENE_VIEW_CREATED = "ViewCreated";
        private static final String CHILD_SCENE_ACTIVITY_CREATED = "ActivityCreated";
        private static final String CHILD_SCENE_START = "Start";
        private static final String CHILD_SCENE_RESUME = "Resume";
        private static final String CHILD_SCENE_PAUSE = "Pause";
        private static final String CHILD_SCENE_STOP = "Stop";
        private static final String CHILD_SCENE_DESTROY = "Destroy";

        public final int mId;

        public TestScene() {
            mId = ViewIdGenerator.generateViewId();

            add(mId, new TestChildScene(), CHILD_SCENE_CONSTRUCTOR);

            List<Scene> list = getSceneList();
            for (Scene scene : list) {
                checkSceneState(scene, State.NONE, scene == findSceneByTag(CHILD_SCENE_CONSTRUCTOR));
            }
        }

        @Override
        public void onAttach() {
            super.onAttach();

            add(mId, new TestChildScene(), CHILD_SCENE_ATTACH);

            List<Scene> list = getSceneList();
            for (Scene scene : list) {
                checkSceneState(scene, State.NONE, scene == findSceneByTag(CHILD_SCENE_CONSTRUCTOR) || scene == findSceneByTag(CHILD_SCENE_ATTACH));
            }
        }

        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            add(mId, new TestChildScene(), CHILD_SCENE_CREATE_VIEW);

            List<Scene> list = getSceneList();
            for (Scene scene : list) {
                checkSceneState(scene, State.NONE,
                        scene == findSceneByTag(CHILD_SCENE_CONSTRUCTOR)
                                || scene == findSceneByTag(CHILD_SCENE_ATTACH)
                                || scene == findSceneByTag(CHILD_SCENE_CREATE_VIEW));
            }

            return new FrameLayout(requireSceneContext());
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            view.setId(mId);

            add(mId, new TestChildScene(), CHILD_SCENE_VIEW_CREATED);

            List<Scene> list = getSceneList();
            for (Scene scene : list) {
                checkSceneState(scene, State.NONE, scene == findSceneByTag(CHILD_SCENE_CONSTRUCTOR)
                        || scene == findSceneByTag(CHILD_SCENE_ATTACH)
                        || scene == findSceneByTag(CHILD_SCENE_CREATE_VIEW)
                        || scene == findSceneByTag(CHILD_SCENE_VIEW_CREATED));
            }
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            assertTrue(View.VISIBLE == getView().getVisibility() || View.GONE == getView().getVisibility());

            add(mId, new TestChildScene(), CHILD_SCENE_ACTIVITY_CREATED);

            List<Scene> list = getSceneList();
            for (Scene scene : list) {
                checkSceneState(scene, State.VIEW_CREATED, false);
            }
        }

        @Override
        public void onStart() {
            super.onStart();
            assertEquals(View.VISIBLE, getView().getVisibility());

            TestChildScene childScene = new TestChildScene();
            add(mId, childScene, CHILD_SCENE_START);

            List<Scene> list = getSceneList();
            for (Scene scene : list) {
                checkSceneState(scene, State.ACTIVITY_CREATED, false);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            assertEquals(View.VISIBLE, getView().getVisibility());

            add(mId, new TestChildScene(), CHILD_SCENE_RESUME);

            List<Scene> list = getSceneList();
            for (Scene scene : list) {
                checkSceneState(scene, State.STARTED, false);
            }
        }

        @Override
        protected void onPostResume() {
            super.onPostResume();
            List<Scene> list = getSceneList();
            for (Scene scene : list) {
                checkSceneState(scene, State.RESUMED, false);
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            assertEquals(View.VISIBLE, getView().getVisibility());

            add(mId, new TestChildScene(), CHILD_SCENE_PAUSE);

            List<Scene> list = getSceneList();
            for (Scene scene : list) {
                checkSceneState(scene, State.STARTED, false);
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            assertEquals(View.VISIBLE, getView().getVisibility());

            add(mId, new TestChildScene(), CHILD_SCENE_STOP);

            List<Scene> list = getSceneList();
            for (Scene scene : list) {
                checkSceneState(scene, State.ACTIVITY_CREATED, false);
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            assertTrue(View.VISIBLE == getView().getVisibility() || View.GONE == getView().getVisibility());

            TestChildScene childScene = new TestChildScene();
            add(mId, childScene, CHILD_SCENE_DESTROY);

            List<Scene> list = getSceneList();
            for (Scene scene : list) {
                checkSceneState(scene, State.NONE, scene == childScene);
            }
        }
    }

    private static void checkSceneState(Scene scene, State state, boolean addedBeforeCreateViewOrAfterDestroy) {
        switch (state) {
            case NONE:
                assertEquals(scene.getState(), State.NONE);
                assertFalse(scene.isVisible());
                assertNull(scene.getView());
                assertNull(scene.getParentScene());
                assertNull(scene.getNavigationScene());
                assertNull(scene.getActivity());
                assertNull(scene.getApplicationContext());
                assertNotNull(scene.getLifecycle());

                if (addedBeforeCreateViewOrAfterDestroy) {
                    assertSame(scene.getLifecycle().getCurrentState(), Lifecycle.State.INITIALIZED);
                } else {
                    assertSame(scene.getLifecycle().getCurrentState(), Lifecycle.State.DESTROYED);
                }
                break;
            case VIEW_CREATED:
                assertEquals(scene.getState(), State.VIEW_CREATED);
                assertFalse(scene.isVisible());
                assertNotNull(scene.getView());
                assertSame(scene.getView().getContext().getSystemService(Scene.SCENE_SERVICE), scene);
                assertNotNull(scene.getActivity());
                assertNotNull(scene.getParentScene());
                assertNotNull(scene.getNavigationScene());
                assertNotNull(scene.getActivity());
                assertNotNull(scene.getApplicationContext());
                assertNotNull(scene.getSceneContext());
                assertNotNull(scene.getLifecycle());
                assertNotNull(scene.getResources());
                assertNotNull(scene.getScope());
                assertNotNull(scene.getLayoutInflater());
                assertNotNull(scene.getViewModelStore());
                assertEquals(scene.getLifecycle().getCurrentState(), Lifecycle.State.INITIALIZED);
                break;
            case ACTIVITY_CREATED:
                assertEquals(scene.getState(), State.ACTIVITY_CREATED);
                assertFalse(scene.isVisible());
                assertNotNull(scene.getView());
                assertSame(scene.getView().getContext().getSystemService(Scene.SCENE_SERVICE), scene);
                assertNotNull(scene.getParentScene());
                assertNotNull(scene.getNavigationScene());
                assertNotNull(scene.getActivity());
                assertNotNull(scene.getApplicationContext());
                assertNotNull(scene.getSceneContext());
                assertNotNull(scene.getLifecycle());
                assertNotNull(scene.getResources());
                assertNotNull(scene.getScope());
                assertNotNull(scene.getLayoutInflater());
                assertNotNull(scene.getViewModelStore());
                assertEquals(scene.getLifecycle().getCurrentState(), Lifecycle.State.CREATED);
                break;
            case STARTED:
                assertEquals(scene.getState(), State.STARTED);
                assertTrue(scene.isVisible());
                assertNotNull(scene.getView());
                assertSame(scene.getView().getContext().getSystemService(Scene.SCENE_SERVICE), scene);
                assertNotNull(scene.getParentScene());
                assertNotNull(scene.getNavigationScene());
                assertNotNull(scene.getActivity());
                assertNotNull(scene.getApplicationContext());
                assertNotNull(scene.getSceneContext());
                assertNotNull(scene.getLifecycle());
                assertNotNull(scene.getResources());
                assertNotNull(scene.getScope());
                assertNotNull(scene.getLayoutInflater());
                assertNotNull(scene.getViewModelStore());
                assertEquals(scene.getLifecycle().getCurrentState(), Lifecycle.State.STARTED);
                break;
            case RESUMED:
                assertEquals(scene.getState(), State.RESUMED);
                assertTrue(scene.isVisible());
                assertNotNull(scene.getView());
                assertSame(scene.getView().getContext().getSystemService(Scene.SCENE_SERVICE), scene);
                assertNotNull(scene.getParentScene());
                assertNotNull(scene.getNavigationScene());
                assertNotNull(scene.getActivity());
                assertNotNull(scene.getApplicationContext());
                assertNotNull(scene.getSceneContext());
                assertNotNull(scene.getLifecycle());
                assertNotNull(scene.getResources());
                assertNotNull(scene.getScope());
                assertNotNull(scene.getLayoutInflater());
                assertNotNull(scene.getViewModelStore());
                assertEquals(scene.getLifecycle().getCurrentState(), Lifecycle.State.RESUMED);
                break;
        }
    }

    public static class TestChildScene extends Scene {
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
