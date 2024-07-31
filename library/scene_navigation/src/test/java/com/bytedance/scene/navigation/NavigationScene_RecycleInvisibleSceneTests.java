package com.bytedance.scene.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

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

        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(scene1);
        Assert.assertEquals(-1, scene1.restoredLocalVar);

        navigationScene.push(scene2);
        navigationScene.forceExecutePendingNavigationOperation();
        Assert.assertEquals(-1, scene2.restoredLocalVar);

        navigationScene.push(scene3);
        navigationScene.forceExecutePendingNavigationOperation();
        Assert.assertEquals(-1, scene3.restoredLocalVar);

        navigationScene.recycleInvisibleScenes();
        navigationScene.forceExecutePendingNavigationOperation();

        List<Scene> scenes = navigationScene.getSceneList();
        Assert.assertEquals(3, scenes.size());

        MyScene afterRecycledScene3 = (MyScene) Objects.requireNonNull(navigationScene.getCurrentScene());
        Assert.assertEquals(3, afterRecycledScene3.localVar);
        Assert.assertEquals(-1, afterRecycledScene3.restoredLocalVar);

        navigationScene.pop();
        navigationScene.forceExecutePendingNavigationOperation();
        MyScene newScene2 = (MyScene) Objects.requireNonNull(navigationScene.getCurrentScene());
        Assert.assertEquals(-1, newScene2.localVar);
        Assert.assertEquals(2, newScene2.restoredLocalVar);
        Assert.assertEquals(newScene2.getArguments().getString("key"), "value2");

        navigationScene.pop();
        navigationScene.forceExecutePendingNavigationOperation();
        MyScene newScene1 = (MyScene) Objects.requireNonNull(navigationScene.getCurrentScene());
        // cause createFromSceneLifecycleManager hold scene1, so newScene1 still be scene1
        Assert.assertEquals(1, newScene1.localVar);
        Assert.assertEquals(1, newScene1.restoredLocalVar);
        Assert.assertEquals(newScene1.getArguments().getString("key"), "value1");
    }
}
