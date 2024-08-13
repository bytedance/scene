package com.bytedance.scene.navigation.post.push;

import static android.os.Looper.getMainLooper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.State;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSourceUtility;
import com.bytedance.scene.navigation.post.NavigationSourceSupportPostUtility;
import com.bytedance.scene.navigation.utility.LogUtility;
import com.bytedance.scene.navigation.utility.RandomLifecycleLogScene;
import com.bytedance.scene.utlity.ViewIdGenerator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

/**
 * Created by jiangqi on 2023/11/27
 *
 * @author jiangqi@bytedance.com
 */

/**
 * four cases
 * <p>
 * 1. non translucent Scene +  non translucent Scene
 * 2. non translucent Scene +  translucent Scene
 * 3. non translucent Scene +  translucent Scene + non translucent Scene
 * 4. non translucent Scene +  translucent Scene + translucent Scene
 *
 * push post + push
 * push post + pop
 *
 * pop post + push
 * pop post + pop
 *
 * push post + push post
 * pop post + pop post
 * push post + pop post
 * pop post + push post
 *
 *
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class Push_Post_Lifecycle_Order {

    /**
     * non translucent Scene +  non translucent Scene
     * push order
     */
    @LooperMode(PAUSED)
    @Test
    public void testPushOrder_NonTranslucent_NonTranslucent() {
        final TestScene groupScene = new TestScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceSupportPostUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        final StringBuilder lifecycleLog = new StringBuilder();

        navigationScene.push(new RandomLifecycleLogScene(lifecycleLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogPause() {
                lifecycleLog.append("0");
            }

            @Override
            public void onLogStop() {
                lifecycleLog.append("4");
            }
        });

        LogUtility.clear(lifecycleLog);

        assertEquals(2, navigationScene.getSceneList().size());

        navigationScene.push(new RandomLifecycleLogScene(lifecycleLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogActivityCreated() {
                lifecycleLog.append("1");
            }

            @Override
            public void onLogStart() {
                lifecycleLog.append("2");
            }

            @Override
            public void onLogResume() {
                lifecycleLog.append("3");
            }
        }, new PushOptions.Builder().setUsePost(true).build());

        assertEquals("0", lifecycleLog.toString());
        assertEquals(2, navigationScene.getSceneList().size());
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getSceneList().get(0).getState());
        assertEquals(State.STARTED, navigationScene.getCurrentScene().getState());

        shadowOf(getMainLooper()).runOneTask();//execute Handler posted task
        assertEquals(3, navigationScene.getSceneList().size());
        assertEquals("0123", lifecycleLog.toString());
        assertEquals(State.STARTED, navigationScene.getSceneList().get(1).getState());
        assertEquals(State.RESUMED, navigationScene.getCurrentScene().getState());

        shadowOf(getMainLooper()).runOneTask();//execute Handler posted task
        assertEquals(3, navigationScene.getSceneList().size());
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getSceneList().get(1).getState());
        assertEquals(State.RESUMED, navigationScene.getCurrentScene().getState());
        assertEquals("01234", lifecycleLog.toString());
    }

    /**
     * non translucent Scene +  non translucent Scene
     * push order
     */
    @LooperMode(PAUSED)
    @Test
    public void testPushOrder_NonTranslucent_NonTranslucent_With_UsePostWhenPause() {
        final TestScene groupScene = new TestScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceSupportPostUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        final StringBuilder lifecycleLog = new StringBuilder();

        navigationScene.push(new RandomLifecycleLogScene(lifecycleLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogPause() {
                lifecycleLog.append("0");
            }

            @Override
            public void onLogStop() {
                lifecycleLog.append("4");
            }
        });

        LogUtility.clear(lifecycleLog);

        assertEquals(2, navigationScene.getSceneList().size());

        navigationScene.push(new RandomLifecycleLogScene(lifecycleLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogActivityCreated() {
                lifecycleLog.append("1");
            }

            @Override
            public void onLogStart() {
                lifecycleLog.append("2");
            }

            @Override
            public void onLogResume() {
                lifecycleLog.append("3");
            }
        }, new PushOptions.Builder().setUsePost(true).setUsePostWhenPause(true).build()); //setUsePostWhenPause(true)

        assertEquals("", lifecycleLog.toString());
        assertEquals(2, navigationScene.getSceneList().size());
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getSceneList().get(0).getState());
        assertEquals(State.RESUMED, navigationScene.getCurrentScene().getState());
        shadowOf(getMainLooper()).runOneTask();//execute Handler posted task

        assertEquals("0", lifecycleLog.toString());
        assertEquals(2, navigationScene.getSceneList().size());
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getSceneList().get(0).getState());
        assertEquals(State.STARTED, navigationScene.getCurrentScene().getState());

        shadowOf(getMainLooper()).runOneTask();//execute Handler posted task
        assertEquals(3, navigationScene.getSceneList().size());
        assertEquals("0123", lifecycleLog.toString());
        assertEquals(State.STARTED, navigationScene.getSceneList().get(1).getState());
        assertEquals(State.RESUMED, navigationScene.getCurrentScene().getState());

        shadowOf(getMainLooper()).runOneTask();//execute Handler posted task
        assertEquals(3, navigationScene.getSceneList().size());
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getSceneList().get(1).getState());
        assertEquals(State.RESUMED, navigationScene.getCurrentScene().getState());
        assertEquals("01234", lifecycleLog.toString());
    }

    /**
     * non translucent Scene + translucent Scene
     * push order
     */
    @LooperMode(PAUSED)
    @Test
    public void testPushOrder_NonTranslucent_Translucent() {
        final TestScene groupScene = new TestScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceSupportPostUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        final StringBuilder lifecycleLog = new StringBuilder();

        navigationScene.push(new RandomLifecycleLogScene(lifecycleLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogPause() {
                lifecycleLog.append("0");
            }

            @Override
            public void onLogStop() {
                lifecycleLog.append("4");
            }
        });

        LogUtility.clear(lifecycleLog);

        assertEquals(2, navigationScene.getSceneList().size());

        navigationScene.push(new RandomLifecycleLogScene(lifecycleLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }

            @Override
            public void onLogActivityCreated() {
                lifecycleLog.append("1");
            }

            @Override
            public void onLogStart() {
                lifecycleLog.append("2");
            }

            @Override
            public void onLogResume() {
                lifecycleLog.append("3");
            }
        }, new PushOptions.Builder().setUsePost(true).setTranslucent(true).build());

        assertEquals("0", lifecycleLog.toString());
        assertEquals(2, navigationScene.getSceneList().size());
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getSceneList().get(0).getState());
        assertEquals(State.STARTED, navigationScene.getCurrentScene().getState());

        shadowOf(getMainLooper()).runOneTask();//execute Handler posted task
        assertEquals(3, navigationScene.getSceneList().size());
        assertEquals("0123", lifecycleLog.toString());
        assertEquals(State.STARTED, navigationScene.getSceneList().get(1).getState());
        assertEquals(State.RESUMED, navigationScene.getCurrentScene().getState());

        shadowOf(getMainLooper()).runOneTask();//execute Handler posted task
        assertEquals(3, navigationScene.getSceneList().size());
        assertEquals(State.STARTED, navigationScene.getSceneList().get(1).getState());
        assertEquals(State.RESUMED, navigationScene.getCurrentScene().getState());
        assertEquals("0123", lifecycleLog.toString());
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
}
