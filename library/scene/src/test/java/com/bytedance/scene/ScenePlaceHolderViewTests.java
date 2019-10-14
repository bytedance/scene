package com.bytedance.scene;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.group.ScenePlaceHolderView;
import com.bytedance.scene.navigation.NavigationScene;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ScenePlaceHolderViewTests {
    @Test
    public void test() {
        final AtomicBoolean called = new AtomicBoolean();
        GroupScene groupScene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return (ViewGroup) inflater.inflate(TestResources.getLayout(this, "layout_place_holder_view"), container, false);
            }

            @Override
            public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);
                ScenePlaceHolderView holderView = requireViewById(TestResources.getId(this, "scene_place_holder"));
                Bundle bundle = new Bundle();
                bundle.putString("key", "value");
                holderView.setArguments(bundle);
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                View targetView = findViewById(TestResources.getId(this, "scene_place_holder"));
                assertNotNull(targetView);
                assertFalse(targetView instanceof ScenePlaceHolderView);
                Scene scene = findSceneByTag("Test_Tag");
                assertNotNull(scene);
                assertTrue(isShow(scene));
                assertNotNull(scene.getArguments());
                assertEquals("value", scene.requireArguments().getString("key"));
                called.compareAndSet(false, true);
            }
        };

        Pair<SceneLifecycleManager, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        assertTrue(called.get());
    }

    @Test
    public void testHidden() {
        final AtomicBoolean called = new AtomicBoolean();
        GroupScene groupScene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return (ViewGroup) inflater.inflate(TestResources.getLayout(this, "layout_place_holder_view"), container, false);
            }

            @Override
            public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);
                ScenePlaceHolderView holderView = requireViewById(TestResources.getId(this, "scene_place_holder"));
                holderView.setVisibility(View.GONE);
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                Scene scene = findSceneByTag("Test_Tag");
                assertNotNull(scene);
                assertFalse(isShow(scene));
                called.compareAndSet(false, true);
            }
        };

        Pair<SceneLifecycleManager, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        assertTrue(called.get());
    }

    @Test(expected = IllegalStateException.class)
    public void testINVISIBLE_Exception() {
        final AtomicBoolean called = new AtomicBoolean();
        GroupScene groupScene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return (ViewGroup) inflater.inflate(TestResources.getLayout(this, "layout_place_holder_view"), container, false);
            }

            @Override
            public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);
                ScenePlaceHolderView holderView = requireViewById(TestResources.getId(this, "scene_place_holder"));
                holderView.setVisibility(View.INVISIBLE);
            }
        };

        Pair<SceneLifecycleManager, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        assertTrue(called.get());
    }
}
