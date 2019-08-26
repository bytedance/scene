package com.bytedance.scene;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.ViewIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GroupSceneExceptionTests {
    @Test(expected = IllegalArgumentException.class)
    public void testAddToSelfException() {
        TestEmptyScene testScene = new TestEmptyScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        testScene.add(testScene.mId, testScene, "childScene");//add GroupScene to itself, throw exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTagNPE() {
        TestEmptyScene testScene = new TestEmptyScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        TestChildScene childScene = new TestChildScene();
        testScene.add(testScene.mId, childScene, null);//tag is null, throw exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTagDuplicateException() {
        TestEmptyScene testScene = new TestEmptyScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        TestChildScene childScene = new TestChildScene();
        testScene.add(testScene.mId, childScene, "childScene");
        testScene.add(testScene.mId, new TestChildScene(), "childScene");//tag is duplicate, throw exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddSceneAlreadyHasParentException() {
        TestEmptyScene testScene = new TestEmptyScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        TestChildScene childScene = new TestChildScene();
        testScene.add(testScene.mId, childScene, "childScene");

        TestEmptyScene testScene2 = new TestEmptyScene();
        testScene2.add(testScene.mId, childScene, "childScene");//childScene already have a parent GroupScene, throw exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddException4() {
        TestEmptyScene testScene = new TestEmptyScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        TestChildScene childScene = new TestChildScene();
        testScene.add(testScene.mId, childScene, "childScene");
        testScene.add(testScene.mId2, childScene, "childScene");//childScene already be added to another ViewGroup, throw exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddException5() {
        TestEmptyScene testScene = new TestEmptyScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        TestChildScene childScene = new TestChildScene();
        testScene.add(testScene.mId, childScene, "childScene");
        testScene.add(testScene.mId, childScene, "childScene2");//childScene already be added, but tag is different, throw exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddToChildSceneViewHierarchyException() {
        TestEmptyScene testScene = new TestEmptyScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(testScene);

        TestEmptyScene childScene = new TestEmptyScene();
        TestChildScene scene = new TestChildScene();
        testScene.add(testScene.mId, childScene, "childScene");
        testScene.add(childScene.mId, scene, "childScene2");//add to childScene's view, throw exception
    }

    @Test
    public void addSameChildSceneMultiple() {
        TestEmptyScene testScene = new TestEmptyScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        TestChildScene childScene = new TestChildScene();
        for (int i = 0; i < 50; i++) {
            testScene.add(testScene.mId, childScene, "childScene");//add childScene again, should not throw exception
            assertEquals(childScene.getState(), State.RESUMED);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveSceneNotFoundException() {
        TestEmptyScene testScene = new TestEmptyScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        TestChildScene childScene = new TestChildScene();
        testScene.remove(childScene);//target Scene not found, throw exception
    }

    @Test(expected = IllegalStateException.class)
    public void testShowSceneNotFoundException() {
        TestEmptyScene testScene = new TestEmptyScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        TestChildScene childScene = new TestChildScene();
        testScene.show(childScene);//target Scene not found, throw exception
    }

    @Test(expected = IllegalStateException.class)
    public void testHideSceneNotFoundException() {
        TestEmptyScene testScene = new TestEmptyScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(testScene);
        TestChildScene childScene = new TestChildScene();
        testScene.hide(childScene);//target Scene not found, throw exception
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

    public static class TestActivity extends Activity {
        public FrameLayout mFrameLayout;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mFrameLayout = new FrameLayout(this);
            setContentView(mFrameLayout);
        }
    }
}
