package com.bytedance.scene.navigation;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.State;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.ViewIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import com.bytedance.scene.Scene;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NavigationSceneLifecycleTests {
    /**
     * non translucent Scene +  non translucent Scene
     */
    @Test
    public void testNavigationSceneLifecycle() {
        final TestScene groupScene = new TestScene();
        TestChildScene secondScene = new TestChildScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);
        assertNotNull(groupScene.getView().getParent());
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

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

    /**
     * stop order, FILO
     */
    @Test
    public void testStopOrder() {
        final TestScene groupScene = new TestScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

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

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

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
            public void onDestroyView() {
                super.onDestroyView();
                closeLog.append("1");
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
                closeLog.append("2");
            }
        });

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onDestroyView();

        assertEquals("210", closeLog.toString());
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

