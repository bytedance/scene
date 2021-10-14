package com.bytedance.scene.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneLifecycleManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * Created by JiangQi on 4/21/21.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SceneTests {
    @Test(expected = IllegalStateException.class)
    public void testRequireNavigationSceneExceptionBeforeAttach() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationSceneGetter.requireNavigationScene(scene);
    }

    @Test(expected = IllegalStateException.class)
    public void testRequireNavigationSceneExceptionOnRootNavigationScene() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(scene);
        NavigationSceneGetter.requireNavigationScene(navigationScene);
    }

    @Test
    public void testRequireNavigationSceneAfterAttach() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene);

        Scene childScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };

        assertNotNull(NavigationSceneGetter.getNavigationScene(scene));
        assertNotNull(NavigationSceneGetter.requireNavigationScene(scene));
        assertSame(pair.second, NavigationSceneGetter.requireNavigationScene(scene));
    }

    @Test(expected = IllegalStateException.class)
    public void testRootSceneRequireNavigationSceneException() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene);

        assertNull(NavigationSceneGetter.getNavigationScene(NavigationSceneGetter.requireNavigationScene(scene)));
        NavigationSceneGetter.requireNavigationScene(NavigationSceneGetter.requireNavigationScene(scene));
    }
}
