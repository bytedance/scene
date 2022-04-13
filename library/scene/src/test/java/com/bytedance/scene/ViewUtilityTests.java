package com.bytedance.scene;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.ViewUtility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ViewUtilityTests {
    @Test
    public void testSceneContext() {
        final TestScene testScene = new TestScene();

        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(testScene);
        View view = testScene.requireView();
        assertEquals(ViewUtility.findSceneByView(view), testScene);

        FrameLayout frameLayout = (FrameLayout) view;
        View childView = new View(testScene.requireSceneContext());
        frameLayout.addView(childView);
        assertEquals(ViewUtility.findSceneByView(childView), testScene);
    }

    @Test
    public void testActivityContext() {
        final TestScene2 testScene = new TestScene2();

        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(testScene);
        View view = testScene.requireView();
        assertEquals(ViewUtility.findSceneByView(view), testScene);

        FrameLayout frameLayout = (FrameLayout) view;
        View childView = new View(testScene.requireSceneContext());
        frameLayout.addView(childView);
        assertEquals(ViewUtility.findSceneByView(childView), testScene);
    }

    public static class TestScene extends GroupScene {
        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new FrameLayout(requireSceneContext());
        }
    }

    public static class TestScene2 extends GroupScene {
        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new FrameLayout(requireActivity());
        }
    }
}
