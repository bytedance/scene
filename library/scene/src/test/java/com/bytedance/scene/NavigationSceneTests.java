package com.bytedance.scene;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.bytedance.scene.group.ReuseGroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.interfaces.PushResultCallback;
import com.bytedance.scene.navigation.NavigationScene;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NavigationSceneTests {
    @Test
    public void testArguments() {
        Scene rootScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(rootScene);
        Bundle bundle = new Bundle();
        bundle.putString("key", "value");
        navigationScene.push(TestScene.class, bundle);
        TestScene testScene = (TestScene) navigationScene.getCurrentScene();
        assertEquals("value", testScene.requireArguments().getString("key"));
    }

    @Test
    public void testTestReuseGroupScene() {
        Scene rootScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(rootScene);
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        TestReuseGroupScene reuseGroupScene = new TestReuseGroupScene();
        navigationScene.push(reuseGroupScene);
        navigationScene.remove(reuseGroupScene);
        navigationScene.push(TestReuseGroupScene.class);
        assertSame(reuseGroupScene, navigationScene.getCurrentScene());
    }

    @Test
    public void testSetResult() {
        Scene rootScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(rootScene);
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        final AtomicReference<String> called = new AtomicReference<>(null);
        navigationScene.push(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                requireNavigationScene().setResult(this, "VALUE");
            }
        }, new PushOptions.Builder().setPushResultCallback(new PushResultCallback() {
            @Override
            public void onResult(@Nullable Object result) {
                called.set((String) result);
            }
        }).build());
        navigationScene.pop();
        assertEquals("VALUE", called.get());
    }

    @Test
    public void testSetResultNullValue() {
        Scene rootScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(rootScene);
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        final AtomicReference<String> called = new AtomicReference<>("INIT");
        navigationScene.push(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        }, new PushOptions.Builder().setPushResultCallback(new PushResultCallback() {
            @Override
            public void onResult(@Nullable Object result) {
                called.set((String) result);
            }
        }).build());
        navigationScene.pop();
        assertNull(called.get());
    }

    public static class TestScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    public static class TestReuseGroupScene extends ReuseGroupScene {
        @NonNull
        @Override
        protected ViewGroup onCreateNewView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new FrameLayout(requireSceneContext());
        }
    }
}
