package com.bytedance.scene.navigation.containersync;

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

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NavigationScene_Sync_Lifecycle_OrderTests {

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

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogStop() {
                closeLog.append("0");
            }
        });

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogStop() {
                closeLog.append("1");
            }
        },new PushOptions.Builder().build());

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogStop() {
                closeLog.append("2");
            }
        },new PushOptions.Builder().build());

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        sceneLifecycleManager.onPause();

        LogUtility.clear(closeLog);

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

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogDestroyView() {
                closeLog.append("0");
            }
        });

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogDestroyView() {
                closeLog.append("1");
            }
        });

        navigationScene.push(new RandomLifecycleLogScene(closeLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogDestroyView() {
                closeLog.append("2");
            }
        });

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();

        LogUtility.clear(closeLog);

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

