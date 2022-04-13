package com.bytedance.scene.navigation;


import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.ViewIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;


import static android.os.Looper.getMainLooper;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
@LooperMode(PAUSED)
public class TrackSceneStateChangeTests {
    @Test
    public void testNormalAdd() {
        TestEmptyScene testScene = new TestEmptyScene();
        Scene childScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                int currentSceneCount = NavigationSceneGetter.getNavigationScene(this).getSceneList().size();
                NavigationSceneGetter.getNavigationScene(this).push(PushChildScene.class);
                assertEquals(currentSceneCount, NavigationSceneGetter.getNavigationScene(this).getSceneList().size());//navigation stack should remain same
            }
        };
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        testScene.add(testScene.mId, childScene, "Test");

        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertTrue(navigationScene.getCurrentScene() instanceof PushChildScene);
    }

    @Test
    public void testScheduleNavigationStackOperationOrder() {
        TestEmptyScene testScene = new TestEmptyScene();
        Scene childScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                int currentSceneCount = NavigationSceneGetter.getNavigationScene(this).getSceneList().size();
                NavigationSceneGetter.getNavigationScene(this).push(PushChildScene.class);
                assertEquals(currentSceneCount, NavigationSceneGetter.getNavigationScene(this).getSceneList().size());//navigation stack should remain same
            }
        };
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        testScene.add(testScene.mId, childScene, "Test");

        navigationScene.push(PushChildScene2.class);

        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertTrue(navigationScene.getCurrentScene() instanceof PushChildScene2);
    }

    @Test
    public void testOnActivityCreateMultiPushNavigationStackOperationOrder() {
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                NavigationSceneGetter.getNavigationScene(this).push(PushChildScene.class);
                NavigationSceneGetter.getNavigationScene(this).push(PushChildScene2.class);
                assertEquals(1, NavigationSceneGetter.getNavigationScene(this).getSceneList().size());//navigation stack should remain same
            }
        });
        shadowOf(getMainLooper()).idle();//execute Handler posted task
        assertEquals(3, navigationScene.getSceneList().size());//navigation stack should remain same
        assertTrue(navigationScene.getCurrentScene() instanceof PushChildScene2);
    }

    public static class TestEmptyScene extends GroupScene {
        public final int mId;

        public TestEmptyScene() {
            mId = ViewIdGenerator.generateViewId();
        }

        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            FrameLayout layout = new FrameLayout(requireSceneContext());
            layout.setId(mId);
            return layout;
        }
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
            int currentSceneCount = NavigationSceneGetter.getNavigationScene(this).getSceneList().size();
            NavigationSceneGetter.getNavigationScene(this).push(PushChildScene.class);
            assertEquals(currentSceneCount, NavigationSceneGetter.getNavigationScene(this).getSceneList().size());//navigation stack should remain same
        }
    }

    public static class PushChildScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    public static class PushChildScene2 extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }
}
