package com.bytedance.scene;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.bytedance.scene.exceptions.PreviousExceptionMistakenlyForceCaughtException;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.ViewIdGenerator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GroupSceneExtremeCasesExceptionTests {

    @Test(expected = ExtremeCaseNPEException.class)
    public void throwExceptionInResume() {
        TestEmptyScene testScene = new TestEmptyScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        testScene.add(testScene.mId, new TestChildScene(), "childScene");//add GroupScene to itself, throw exception
    }

    @Test(expected = PreviousExceptionMistakenlyForceCaughtException.class)
    public void throwExceptionInCheckStateChange() {
        TestEmptyScene testScene = new TestEmptyScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        TestChildScene testChildScene = new TestChildScene();
        try {
            testScene.add(testScene.mId, testChildScene, "childScene");//add GroupScene to itself, throw exception
        } catch (ExtremeCaseNPEException ignored) {

        }
        //checkStateChange will throw exception
        testScene.hide(testChildScene);
    }

    @Test(expected = PreviousExceptionMistakenlyForceCaughtException.class)
    public void throwExceptionInBeginTrackSceneStateChange() {
        TestEmptyScene testScene = new TestEmptyScene();
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromSceneLifecycleManagerWithManager(testScene);
        SceneLifecycleManager<GroupScene> sceneLifecycleManager = pair.first;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        TestChildScene testChildScene = new TestChildScene();
        try {
            testScene.add(testScene.mId, testChildScene, "childScene");//add GroupScene to itself, throw exception
        } catch (ExtremeCaseNPEException ignored) {

        }
        //beginTrackSceneStateChange will throw exception
        sceneLifecycleManager.onPause();
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

        @Override
        public void onResume() {
            super.onResume();
            if (true) {
                throw new ExtremeCaseNPEException("Crash");
            }
        }
    }

    public static class TestActivity extends Activity {
        public FrameLayout mFrameLayout;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mFrameLayout = new FrameLayout(this);
            setContentView(mFrameLayout);
        }
    }

    public static class ExtremeCaseNPEException extends RuntimeException {
        public ExtremeCaseNPEException(String s) {
            super(s);
        }
    }
}
