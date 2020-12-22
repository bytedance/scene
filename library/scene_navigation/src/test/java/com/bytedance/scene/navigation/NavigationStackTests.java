package com.bytedance.scene.navigation;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.core.util.Pair;
import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.State;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.ViewIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.State;

import static org.junit.Assert.assertEquals;

/**
 * Push Pop PopTo PopToRoot PushOptions
 **/

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NavigationStackTests {
    @Test
    public void testPushPop() {
        final TestScene rootScene = new TestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        assertEquals(rootScene.getState(), State.RESUMED);

        TestChildScene scene0 = new TestChildScene();

        assertEquals(scene0.getState(), State.NONE);
        assertEquals(rootScene.getState(), State.RESUMED);

        navigationScene.push(scene0);
        assertEquals(scene0.getState(), State.RESUMED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        navigationScene.pop();
        assertEquals(scene0.getState(), State.NONE);
        assertEquals(rootScene.getState(), State.RESUMED);
    }

    @Test
    public void testPopToRoot() {
        final TestScene rootScene = new TestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        assertEquals(rootScene.getState(), State.RESUMED);

        TestChildScene scene0 = new TestChildScene();
        TestChildScene scene1 = new TestChildScene();
        TestChildScene scene2 = new TestChildScene();

        assertEquals(scene0.getState(), State.NONE);
        assertEquals(scene1.getState(), State.NONE);
        assertEquals(scene2.getState(), State.NONE);

        navigationScene.push(scene0);
        navigationScene.push(scene1);
        navigationScene.push(scene2);

        assertEquals(scene2.getState(), State.RESUMED);
        assertEquals(scene1.getState(), State.ACTIVITY_CREATED);
        assertEquals(scene0.getState(), State.ACTIVITY_CREATED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        navigationScene.popToRoot();
        assertEquals(scene0.getState(), State.NONE);
        assertEquals(scene1.getState(), State.NONE);
        assertEquals(scene2.getState(), State.NONE);
        assertEquals(rootScene.getState(), State.RESUMED);
    }

    @Test
    public void testRemove() {
        final TestScene rootScene = new TestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(rootScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        assertEquals(rootScene.getState(), State.RESUMED);

        TestChildScene scene0 = new TestChildScene();
        TestChildScene scene1 = new TestChildScene();
        TestChildScene scene2 = new TestChildScene();

        assertEquals(scene0.getState(), State.NONE);
        assertEquals(scene1.getState(), State.NONE);
        assertEquals(scene2.getState(), State.NONE);

        navigationScene.push(scene0);
        navigationScene.push(scene1);
        navigationScene.push(scene2);

        assertEquals(scene2.getState(), State.RESUMED);
        assertEquals(scene1.getState(), State.ACTIVITY_CREATED);
        assertEquals(scene0.getState(), State.ACTIVITY_CREATED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        navigationScene.remove(scene1);
        assertEquals(scene0.getState(), State.ACTIVITY_CREATED);
        assertEquals(scene1.getState(), State.NONE);
        assertEquals(scene2.getState(), State.RESUMED);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        navigationScene.remove(scene2);
        assertEquals(scene0.getState(), State.RESUMED);
        assertEquals(scene1.getState(), State.NONE);
        assertEquals(scene2.getState(), State.NONE);
        assertEquals(rootScene.getState(), State.ACTIVITY_CREATED);

        navigationScene.remove(rootScene);
        assertEquals(scene0.getState(), State.RESUMED);
        assertEquals(scene1.getState(), State.NONE);
        assertEquals(scene2.getState(), State.NONE);
        assertEquals(rootScene.getState(), State.NONE);
    }

    @Test
    public void testPopTo() {
        final TestScene groupScene = new TestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        TestChildScene secondScene = new TestChildScene();
        TestChild0Scene thirdScene = new TestChild0Scene();

        navigationScene.push(secondScene);
        navigationScene.push(thirdScene);

        assertEquals(thirdScene.getState(), State.RESUMED);
        assertEquals(secondScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(3, navigationScene.getSceneList().size());

        navigationScene.popTo(TestChildScene.class);
        assertEquals(thirdScene.getState(), State.NONE);
        assertEquals(secondScene.getState(), State.RESUMED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(2, navigationScene.getSceneList().size());
    }

    @Test
    public void testPushReplacePredicate() {
        final TestScene groupScene = new TestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        TestChildScene secondScene = new TestChildScene();

        navigationScene.push(secondScene);
        assertEquals(secondScene.getState(), State.RESUMED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(2, navigationScene.getSceneList().size());

        TestChild0Scene thirdScene = new TestChild0Scene();
        navigationScene.push(thirdScene, new PushOptions.Builder().setRemovePredicate(new PushOptions.ReplacePredicate()).build());
        assertEquals(thirdScene.getState(), State.RESUMED);
        assertEquals(secondScene.getState(), State.NONE);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(2, navigationScene.getSceneList().size());
    }

    @Test
    public void testPushClearTaskPredicate() {
        final TestScene groupScene = new TestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        TestChildScene secondScene = new TestChildScene();

        navigationScene.push(secondScene);

        assertEquals(secondScene.getState(), State.RESUMED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(2, navigationScene.getSceneList().size());

        TestChild0Scene thirdScene = new TestChild0Scene();
        navigationScene.push(thirdScene, new PushOptions.Builder().setRemovePredicate(new PushOptions.ClearTaskPredicate()).build());
        assertEquals(thirdScene.getState(), State.RESUMED);
        assertEquals(secondScene.getState(), State.NONE);
        assertEquals(groupScene.getState(), State.NONE);
        assertEquals(1, navigationScene.getSceneList().size());
        assertEquals(thirdScene, navigationScene.getCurrentScene());

        navigationScene.push(groupScene, new PushOptions.Builder().setRemovePredicate(new PushOptions.ClearTaskPredicate()).build());
        assertEquals(1, navigationScene.getSceneList().size());
        assertEquals(groupScene, navigationScene.getCurrentScene());
    }

    @Test
    public void testPushSingleTopPredicate() {
        final TestScene groupScene = new TestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        TestChildScene secondScene = new TestChildScene();

        navigationScene.push(secondScene);

        assertEquals(secondScene.getState(), State.RESUMED);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(2, navigationScene.getSceneList().size());

        TestChildScene thirdScene = new TestChildScene();
        navigationScene.push(thirdScene, new PushOptions.Builder().setRemovePredicate(new PushOptions.SingleTopPredicate(TestChildScene.class)).build());
        assertEquals(thirdScene.getState(), State.RESUMED);
        assertEquals(secondScene.getState(), State.NONE);
        assertEquals(groupScene.getState(), State.ACTIVITY_CREATED);
        assertEquals(2, navigationScene.getSceneList().size());
        assertEquals(thirdScene, navigationScene.getCurrentScene());
    }

    @Test
    public void testPushCountPredicate() {
        final TestScene groupScene = new TestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        for (int i = 0; i < 100; i++) {
            navigationScene.push(CountChildScene.class);
        }
        assertEquals(101, navigationScene.getSceneList().size());

        navigationScene.push(CountChildScene.class, null, new PushOptions.Builder().setRemovePredicate(new PushOptions.CountPredicate(38)).build());
        assertEquals(101 - 38 + 1, navigationScene.getSceneList().size());
    }

    @Test
    public void testPushSingleTaskPredicate() {
        final TestScene groupScene = new TestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        navigationScene.push(TestChildScene.class);

        for (int i = 0; i < 100; i++) {
            navigationScene.push(CountChildScene.class);
        }
        assertEquals(102, navigationScene.getSceneList().size());

        navigationScene.push(TestChildScene.class, null, new PushOptions.Builder().setRemovePredicate(new PushOptions.SingleTaskPredicate(TestChildScene.class)).build());
        assertEquals(102, navigationScene.getSceneList().size());
    }

    @Test
    public void testPopCountUtilPredicate() {
        final TestScene groupScene = new TestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        for (int i = 0; i < 100; i++) {
            navigationScene.push(CountChildScene.class);
        }
        assertEquals(101, navigationScene.getSceneList().size());

        navigationScene.pop(new PopOptions.Builder().setPopUtilPredicate(new PopOptions.CountUtilPredicate(50)).build());
        assertEquals(51, navigationScene.getSceneList().size());

        navigationScene.pop(new PopOptions.Builder().setPopUtilPredicate(new PopOptions.CountUtilPredicate(50)).build());
        assertEquals(1, navigationScene.getSceneList().size());
        assertEquals(groupScene, navigationScene.getCurrentScene());
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

    public static class TestChild0Scene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    public static class SingleTopChildScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    public static class CountChildScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
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
