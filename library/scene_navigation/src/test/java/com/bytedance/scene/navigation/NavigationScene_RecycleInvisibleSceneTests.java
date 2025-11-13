package com.bytedance.scene.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneGlobalConfig;
import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.interfaces.PushOptions;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NavigationScene_RecycleInvisibleSceneTests {

    public static class MyScene extends Scene {

        public int localVar = -1;

        public int restoredLocalVar = -1;

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            if (savedInstanceState != null) {
                restoredLocalVar = savedInstanceState.getInt("localVar", -1);
            }
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("localVar", localVar);
        }
    }

    @Test
    public void testRecycleInvisibleScenes() {
        SceneGlobalConfig.cancelAnimationWhenForceExecutePendingNavigationOperation = true;
        List<MyScene> list = createScenes();
        MyScene scene1 = list.get(0);
        MyScene scene2 = list.get(1);
        MyScene scene3 = list.get(2);

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = createAndPushScene(scene1, scene2, scene3, false);
        NavigationScene navigationScene = pair.second;

        navigationScene.recycleInvisibleScenes();
        navigationScene.forceExecutePendingNavigationOperation();

        assertMyScene3NotRecycle(navigationScene);
        navigationScene.pop();
        navigationScene.forceExecutePendingNavigationOperation();
        assertMyScene2Recycled(navigationScene);
        navigationScene.pop();
        navigationScene.forceExecutePendingNavigationOperation();
        assertMyScene1NotRecycle(navigationScene);
        SceneGlobalConfig.cancelAnimationWhenForceExecutePendingNavigationOperation = false;
    }

    @Test
    public void testRecycleInvisibleScenes_onStop() {
        List<MyScene> list = createScenes();
        MyScene scene1 = list.get(0);
        MyScene scene2 = list.get(1);
        MyScene scene3 = list.get(2);

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = createAndPushScene(scene1, scene2, scene3, false);
        SceneLifecycleManager<?> sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        // call onStop
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();

        navigationScene.recycleInvisibleScenes();
        navigationScene.forceExecutePendingNavigationOperation();

        assertMyScene3NotRecycle(navigationScene);
        navigationScene.pop();
        navigationScene.forceExecutePendingNavigationOperation();
        assertMyScene2Recycled(navigationScene);
        navigationScene.pop();
        navigationScene.forceExecutePendingNavigationOperation();
        assertMyScene1NotRecycle(navigationScene);
    }


    @Test
    public void testRecycleInvisibleScenes_TopSceneTranslucent() {
        SceneGlobalConfig.cancelAnimationWhenForceExecutePendingNavigationOperation = true;
        List<MyScene> list = createScenes();
        MyScene scene1 = list.get(0);
        MyScene scene2 = list.get(1);
        MyScene scene3 = list.get(2);

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = createAndPushScene(scene1, scene2, scene3, true);
        NavigationScene navigationScene = pair.second;

        navigationScene.recycleInvisibleScenes();
        navigationScene.forceExecutePendingNavigationOperation();

        List<Scene> scenes = navigationScene.getSceneList();
        Assert.assertEquals(3, scenes.size());

        assertMyScene3NotRecycle(navigationScene);

        navigationScene.pop();
        navigationScene.forceExecutePendingNavigationOperation();

        assertMyScene2NotRecycle(navigationScene, scene2);

        navigationScene.pop();
        navigationScene.forceExecutePendingNavigationOperation();

        assertMyScene1NotRecycle(navigationScene);
        SceneGlobalConfig.cancelAnimationWhenForceExecutePendingNavigationOperation = false;
    }

    @Test
    public void testSuppressRecycle() {
        SceneGlobalConfig.cancelAnimationWhenForceExecutePendingNavigationOperation = true;

        List<MyScene> list = createScenes();
        MyScene scene1 = list.get(0);
        MyScene scene2 = list.get(1);
        MyScene scene3 = list.get(2);

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = createAndPushScene(scene1, scene2, scene3, false);
        NavigationScene navigationScene = pair.second;

        navigationScene.suppressRecycle(true);
        navigationScene.recycleInvisibleScenes();

        List<Scene> scenes = navigationScene.getSceneList();
        Assert.assertEquals(3, scenes.size());

        assertMyScene3NotRecycle(navigationScene);
        Assert.assertNotNull(scenes.get(1).getView());
        Assert.assertNotNull(scenes.get(0).getView());

        navigationScene.suppressRecycle(false);
        navigationScene.recycleInvisibleScenes();

        scenes = navigationScene.getSceneList();
        Assert.assertEquals(3, scenes.size());

        assertMyScene3NotRecycle(navigationScene);
        Assert.assertNull(scenes.get(1).getView());
        Assert.assertNull(scenes.get(0).getView());

        SceneGlobalConfig.cancelAnimationWhenForceExecutePendingNavigationOperation = false;
    }

    @Test
    public void testRecycleInvisibleScenes_TopSceneTranslucent_onStop() {
        List<MyScene> list = createScenes();
        MyScene scene1 = list.get(0);
        MyScene scene2 = list.get(1);
        MyScene scene3 = list.get(2);

        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = createAndPushScene(scene1, scene2, scene3, true);
        SceneLifecycleManager<?> sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        // call onStop
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();

        navigationScene.recycleInvisibleScenes();
        navigationScene.forceExecutePendingNavigationOperation();

        List<Scene> scenes = navigationScene.getSceneList();
        Assert.assertEquals(3, scenes.size());

        assertMyScene3NotRecycle(navigationScene);

        navigationScene.pop();
        navigationScene.forceExecutePendingNavigationOperation();

        assertMyScene2NotRecycle(navigationScene, scene2);

        navigationScene.pop();
        navigationScene.forceExecutePendingNavigationOperation();

        assertMyScene1NotRecycle(navigationScene);
    }

    private void assertMyScene1NotRecycle(NavigationScene navigationScene) {
        MyScene newScene1 = (MyScene) Objects.requireNonNull(navigationScene.getCurrentScene());
        // cause createFromSceneLifecycleManager hold scene1, so newScene1 still be scene1
        Assert.assertEquals(1, newScene1.localVar);
        Assert.assertEquals(1, newScene1.restoredLocalVar);
        Assert.assertEquals(newScene1.getArguments().getString("key"), "value1");
    }

    private void assertMyScene2Recycled(NavigationScene navigationScene) {
        MyScene newScene2 = (MyScene) Objects.requireNonNull(navigationScene.getCurrentScene());
        Assert.assertEquals(-1, newScene2.localVar);
        Assert.assertEquals(2, newScene2.restoredLocalVar);
        Assert.assertEquals(newScene2.getArguments().getString("key"), "value2");
    }

    private void assertMyScene2NotRecycle(NavigationScene navigationScene, MyScene scene2) {
        MyScene newScene2 = (MyScene) Objects.requireNonNull(navigationScene.getCurrentScene());
        // scene2 is not recycle
        Assert.assertEquals(scene2, newScene2);
        Assert.assertEquals(2, newScene2.localVar);
        Assert.assertEquals(-1, newScene2.restoredLocalVar);
        Assert.assertEquals(newScene2.getArguments().getString("key"), "value2");
    }

    private void assertMyScene3NotRecycle(NavigationScene navigationScene) {
        MyScene afterRecycledScene3 = (MyScene) Objects.requireNonNull(navigationScene.getCurrentScene());
        Assert.assertEquals(3, afterRecycledScene3.localVar);
        Assert.assertEquals(-1, afterRecycledScene3.restoredLocalVar);
    }

    private List<MyScene> createScenes() {
        MyScene scene1 = new MyScene();
        Bundle args1 = new Bundle();
        args1.putString("key", "value1");
        scene1.setArguments(args1);
        scene1.localVar = 1;

        MyScene scene2 = new MyScene();
        scene2.localVar = 2;
        Bundle args2 = new Bundle();
        args2.putString("key", "value2");
        scene2.setArguments(args2);

        MyScene scene3 = new MyScene();
        scene3.localVar = 3;
        Bundle args3 = new Bundle();
        args3.putString("key", "value3");
        scene3.setArguments(args3);

        ArrayList<MyScene> arrayList = new ArrayList<>();
        arrayList.add(scene1);
        arrayList.add(scene2);
        arrayList.add(scene3);
        return arrayList;
    }

    private Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> createAndPushScene(
            MyScene scene1,
            MyScene scene2,
            MyScene scene3,
            boolean scene3Translucent
    ) {
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene1);
        SceneLifecycleManager<?> sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        Assert.assertEquals(-1, scene1.restoredLocalVar);

        navigationScene.push(scene2);
        navigationScene.forceExecutePendingNavigationOperation();
        Assert.assertEquals(-1, scene2.restoredLocalVar);

        navigationScene.push(scene3, new PushOptions.Builder().setTranslucent(scene3Translucent).build());
        navigationScene.forceExecutePendingNavigationOperation();
        Assert.assertEquals(-1, scene3.restoredLocalVar);
        return pair;
    }
}
