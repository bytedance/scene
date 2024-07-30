package com.bytedance.scene.navigation.pop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.State;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSourceUtility;
import com.bytedance.scene.navigation.utility.LogUtility;
import com.bytedance.scene.navigation.utility.RandomLifecycleLogScene;
import com.bytedance.scene.utlity.ViewIdGenerator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * four cases
 * <p>
 * 1. non translucent Scene +  non translucent Scene
 * 2. non translucent Scene +  translucent Scene
 * 3. non translucent Scene +  translucent Scene + non translucent Scene
 * 4. non translucent Scene +  translucent Scene + translucent Scene
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NavigationScene_Pop_Lifecycle_Order_UseActivityCompatibleLifecycle {
    /**
     * non translucent Scene +  non translucent Scene
     */
    @Test
    public void testNavigationSceneLifecycle() {
        final TestScene groupScene = new TestScene();
        TestChildScene secondScene = new TestChildScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);//make sure not be affected by animation

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);

        View secondSceneView = secondScene.getView();
        navigationScene.pop(new PopOptions.Builder().setUseActivityCompatibleLifecycle(true).build());
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
        navigationScene.pop(new PopOptions.Builder().setUseActivityCompatibleLifecycle(true).build());
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
        navigationScene.pop(new PopOptions.Builder().setUseActivityCompatibleLifecycle(true).build());
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
        navigationScene.pop(new PopOptions.Builder().setUseActivityCompatibleLifecycle(true).build());
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
        navigationScene.pop(new PopOptions.Builder().setUseActivityCompatibleLifecycle(true).build());
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
     * non translucent Scene +  non translucent Scene
     */
    @Test
    public void testPopLifecycleOrder_NonTranslucent_NonTranslucent() {
        final TestScene groupScene = new TestScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        final StringBuilder closeLog = new StringBuilder();

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogStart() {
                closeLog.append("1");
            }

            @Override
            public void onLogResume() {
                closeLog.append("2");
            }
        });

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogPause() {
                closeLog.append("0");
            }

            @Override
            public void onLogStop() {
                closeLog.append("3");
            }

            @Override
            public void onLogDestroyView() {
                closeLog.append("4");
            }

        },new PushOptions.Builder().build());

        closeLog.delete(0, closeLog.length());

        navigationScene.pop(new PopOptions.Builder().setUseActivityCompatibleLifecycle(true).build());

        assertEquals("01234", closeLog.toString());
        assertEquals(navigationScene.getSceneList().size(), 2);
        assertEquals(navigationScene.getCurrentScene().getState(), State.RESUMED);
        assertEquals(navigationScene.getSceneList().get(0).getState(), State.ACTIVITY_CREATED);
    }

    /**
     * non translucent Scene +  translucent Scene
     */
    @Test
    public void testPopLifecycleOrder_NonTranslucent_Translucent() {
        final TestScene groupScene = new TestScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        final StringBuilder closeLog = new StringBuilder();

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogStart() {
                closeLog.append("1");
            }

            @Override
            public void onLogResume() {
                closeLog.append("2");
            }
        });

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogPause() {
                closeLog.append("0");
            }

            @Override
            public void onLogStop() {
                closeLog.append("3");
            }

            @Override
            public void onLogDestroyView() {
                closeLog.append("4");
            }

        },new PushOptions.Builder().setTranslucent(true).build());

        closeLog.delete(0, closeLog.length());

        navigationScene.pop(new PopOptions.Builder().setUseActivityCompatibleLifecycle(true).build());

        // without 1
        assertEquals("0234", closeLog.toString());
        assertEquals(navigationScene.getSceneList().size(), 2);
        assertEquals(navigationScene.getCurrentScene().getState(), State.RESUMED);
        assertEquals(navigationScene.getSceneList().get(0).getState(), State.ACTIVITY_CREATED);
    }


    /**
     * non translucent Scene +  translucent Scene + non translucent Scene
     */
    @Test
    public void testPopLifecycleOrder_NonTranslucent_Translucent_NonTranslucent() {
        final TestScene groupScene = new TestScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        final StringBuilder closeLog = new StringBuilder();

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogStart() {
                closeLog.append("3");
            }
        });

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogStart() {
                closeLog.append("1");
            }

            @Override
            public void onLogResume() {
                closeLog.append("2");
            }

        },new PushOptions.Builder().setTranslucent(true).build());

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogPause() {
                closeLog.append("0");
            }

            @Override
            public void onLogStop() {
                closeLog.append("4");
            }

            @Override
            public void onLogDestroyView() {
                closeLog.append("5");
            }
        });

        closeLog.delete(0, closeLog.length());

        navigationScene.pop(new PopOptions.Builder().setUseActivityCompatibleLifecycle(true).build());

        assertEquals("012345", closeLog.toString());
        assertEquals(navigationScene.getSceneList().size(), 3);
        assertEquals(navigationScene.getCurrentScene().getState(), State.RESUMED);
        assertEquals(navigationScene.getSceneList().get(1).getState(), State.STARTED);
        assertEquals(navigationScene.getSceneList().get(0).getState(), State.ACTIVITY_CREATED);
    }

    /**
     * non translucent Scene +  translucent Scene + translucent Scene
     */
    @Test
    public void testPopLifecycleOrder_NonTranslucent_Translucent_Translucent() {
        final TestScene groupScene = new TestScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        final StringBuilder closeLog = new StringBuilder();

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogStart() {
                closeLog.append("3");
            }
        });

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogStart() {
                closeLog.append("1");
            }

            @Override
            public void onLogResume() {
                closeLog.append("2");
            }

        },new PushOptions.Builder().setTranslucent(true).build());

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogPause() {
                closeLog.append("0");
            }

            @Override
            public void onLogStop() {
                closeLog.append("4");
            }

            @Override
            public void onLogDestroyView() {
                closeLog.append("5");
            }
        },new PushOptions.Builder().setTranslucent(true).build());

        LogUtility.clear(closeLog);

        navigationScene.pop(new PopOptions.Builder().setUseActivityCompatibleLifecycle(true).build());

        assertEquals("0245", closeLog.toString());
        assertEquals(navigationScene.getSceneList().size(), 3);
        assertEquals(navigationScene.getCurrentScene().getState(), State.RESUMED);
        assertEquals(navigationScene.getSceneList().get(1).getState(), State.STARTED);
        assertEquals(navigationScene.getSceneList().get(0).getState(), State.ACTIVITY_CREATED);
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

