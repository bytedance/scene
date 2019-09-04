package com.bytedance.scene;


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

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SceneTests {
    @Test
    public void testNonNullArguments() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        Bundle bundle = new Bundle();
        scene.setArguments(bundle);
        assertSame(bundle, scene.requireArguments());
        assertSame(bundle, scene.getArguments());
    }

    @Test(expected = IllegalStateException.class)
    public void testRequireArgumentsException() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        scene.requireArguments();
    }

    @Test
    public void testRequireViewAfterOnCreateView() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        Pair<SceneLifecycleManager, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene);
        assertNotNull(scene.getView());
        assertNotNull(scene.requireView());
    }

    @Test(expected = IllegalStateException.class)
    public void testRequireViewExceptionBeforeOnCreateView() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        scene.requireView();
    }

    @Test(expected = IllegalStateException.class)
    public void testRequireParentSceneExceptionBeforeAttach() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        scene.requireParentScene();
    }

    @Test
    public void testRequireParentSceneAfterAttach() {
        GroupScene scene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }
        };
        Pair<SceneLifecycleManager, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene);

        Scene childScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };

        scene.requireView().setId(ViewIdGenerator.generateViewId());
        scene.add(scene.requireView().getId(), childScene, "TAG");
        assertNotNull(scene.getParentScene());
        assertNotNull(scene.requireParentScene());
    }

    @Test(expected = IllegalStateException.class)
    public void testRootSceneRequireParentSceneException() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        Pair<SceneLifecycleManager, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene);

        assertNull(scene.getNavigationScene().getParentScene());
        scene.getNavigationScene().requireParentScene();
    }
}
