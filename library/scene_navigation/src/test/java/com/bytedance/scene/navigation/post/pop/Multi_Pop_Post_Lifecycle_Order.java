package com.bytedance.scene.navigation.post.pop;

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
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.post.NavigationSourceSupportPostUtility;
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
 * <p>
 */

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class Multi_Pop_Post_Lifecycle_Order {
    @LooperMode(PAUSED)
    @Test
    public void test() {
        final TestScene groupScene = new TestScene();

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceSupportPostUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        final StringBuilder lifecycleLog = new StringBuilder();

        assertEquals(1, navigationScene.getSceneList().size());
        assertEquals(State.RESUMED, navigationScene.getSceneList().get(0).getState());

        navigationScene.push(new RandomLifecycleLogScene(lifecycleLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }
        }, new PushOptions.Builder().setUsePost(false).build());

        assertEquals(2, navigationScene.getSceneList().size());
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getSceneList().get(0).getState());
        assertEquals(State.RESUMED, navigationScene.getSceneList().get(1).getState());

        navigationScene.push(new RandomLifecycleLogScene(lifecycleLog) {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }
        }, new PushOptions.Builder().setUsePost(false).build());

        assertEquals(3, navigationScene.getSceneList().size());
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getSceneList().get(0).getState());
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getSceneList().get(1).getState());
        assertEquals(State.RESUMED, navigationScene.getSceneList().get(2).getState());

        navigationScene.pop(new PopOptions.Builder().setUsePost(true).build());
        assertEquals(3, navigationScene.getSceneList().size());
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getSceneList().get(0).getState());
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getSceneList().get(1).getState());
        assertEquals(State.STARTED, navigationScene.getSceneList().get(2).getState());

        navigationScene.pop(new PopOptions.Builder().setUsePost(true).build());
        assertEquals(2, navigationScene.getSceneList().size());
        assertEquals(State.ACTIVITY_CREATED, navigationScene.getSceneList().get(0).getState());
        assertEquals(State.STARTED, navigationScene.getSceneList().get(1).getState());

        shadowOf(getMainLooper()).runOneTask();//execute Handler posted task
        assertEquals(2, navigationScene.getSceneList().size());
        assertEquals(State.RESUMED, navigationScene.getSceneList().get(0).getState());
        assertEquals(State.STARTED, navigationScene.getSceneList().get(1).getState());

        shadowOf(getMainLooper()).runOneTask();//execute Handler posted task
        assertEquals(1, navigationScene.getSceneList().size());
        assertEquals(State.RESUMED, navigationScene.getSceneList().get(0).getState());
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
