package com.bytedance.scene;


import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import com.bytedance.scene.exceptions.IllegalLifecycleException;
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
    public void testAddBeforeGroupSceneAttach() {
        TestEmptyScene testScene = new TestEmptyScene();
        TestEmptyScene childScene = new TestEmptyScene();
        testScene.add(testScene.mId, childScene, "Test");
    }

    @Test
    public void testAddAfterGroupSceneDetach() {
        TestEmptyScene testScene = new TestEmptyScene();
        TestEmptyScene childScene = new TestEmptyScene();
        SceneLifecycleManager<GroupScene> manager = NavigationSourceUtility.createFromInitSceneLifecycleManager(testScene).first;
        manager.onStart();
        manager.onResume();
        manager.onPause();
        manager.onStop();
        manager.onDestroyView();
        testScene.add(testScene.mId, childScene, "Test");
    }

    @Test(expected = IllegalLifecycleException.class)
    public void testThrowExceptionWhenInvokeParentHideInChildSceneOnActivityCreated() {
        TestEmptyScene testScene = new TestEmptyScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        testScene.add(testScene.mId, new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                ((GroupScene) getParentScene()).hide(this);//crash
            }
        }, "Test");
    }

    @Test
    public void testModifyBrotherSceneStateInChildSceneOnActivityCreated() {
        TestEmptyScene testScene = new TestEmptyScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(testScene);

        final TestEmptyScene brotherScene = new TestEmptyScene();
        testScene.add(testScene.mId, brotherScene, "Test");

        Scene otherScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                ((GroupScene) getParentScene()).hide(brotherScene);
                assertEquals(State.ACTIVITY_CREATED, brotherScene.getState());
            }
        };
        testScene.add(testScene.mId, otherScene, "OtherScene");
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
}
