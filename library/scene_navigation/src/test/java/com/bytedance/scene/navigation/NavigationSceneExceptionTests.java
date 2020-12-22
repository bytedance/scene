package com.bytedance.scene.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.bytedance.scene.Scene;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.ViewIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NavigationSceneExceptionTests {
    @Test(expected = IllegalArgumentException.class)
    public void testException() {
        TestEmptyScene testScene = new TestEmptyScene();
        TestChildScene childScene = new TestChildScene();
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        testScene.add(testScene.mId, childScene, "childScene");
        navigationScene.push(childScene);//childScene already be added to another GroupScene, throw exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException2() {
        TestChildScene childScene = new TestChildScene();
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(childScene);
        navigationScene.push(childScene);//childScene already be pushed, throw exception
    }

    public static class TestEmptyScene extends GroupScene {
        public final int mId;
        public final int mId2;

        public TestEmptyScene() {
            mId = ViewIdGenerator.generateViewId();
            mId2 = ViewIdGenerator.generateViewId();
        }

        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            FrameLayout layout = new FrameLayout(requireSceneContext());
            layout.setId(mId);

            FrameLayout child = new FrameLayout(requireSceneContext());
            child.setId(mId2);
            layout.addView(child, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return layout;
        }
    }

    public static class TestChildScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }
}
