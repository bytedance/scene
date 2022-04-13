package com.bytedance.scene;

import android.view.View;
import androidx.core.util.Pair;

import com.bytedance.scene.group.GroupScene;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GroupSceneTransactionTests {
    @Test
    public void testTransactionAddAndHide() {
        GroupSceneLifecycleTests.TestEmptyScene testScene = new GroupSceneLifecycleTests.TestEmptyScene();
        GroupSceneLifecycleTests.TestChildScene childScene = new GroupSceneLifecycleTests.TestChildScene();

        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(testScene);

        testScene.beginTransaction();
        testScene.add(testScene.mId, childScene, "childScene");
        testScene.hide(childScene);
        testScene.commitTransaction();

        assertEquals(State.ACTIVITY_CREATED, childScene.getState());
        assertEquals(View.GONE, childScene.getView().getVisibility());
        assertFalse(testScene.isShow(childScene));
    }

    @Test
    public void testTransactionHide() {
        GroupSceneLifecycleTests.TestEmptyScene testScene = new GroupSceneLifecycleTests.TestEmptyScene();
        GroupSceneLifecycleTests.TestChildScene childScene = new GroupSceneLifecycleTests.TestChildScene();

        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(testScene);

        testScene.add(testScene.mId, childScene, "childScene");
        assertEquals(View.VISIBLE, childScene.getView().getVisibility());

        testScene.beginTransaction();
        testScene.hide(childScene);
        testScene.commitTransaction();

        assertEquals(State.ACTIVITY_CREATED, childScene.getState());
        assertEquals(View.GONE, childScene.getView().getVisibility());
        assertFalse(testScene.isShow(childScene));
    }

    @Test
    public void testTransactionShow() {
        GroupSceneLifecycleTests.TestEmptyScene testScene = new GroupSceneLifecycleTests.TestEmptyScene();
        GroupSceneLifecycleTests.TestChildScene childScene = new GroupSceneLifecycleTests.TestChildScene();

        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(testScene);

        testScene.add(testScene.mId, childScene, "childScene");
        assertEquals(View.VISIBLE, childScene.getView().getVisibility());

        testScene.hide(childScene);
        assertEquals(View.GONE, childScene.getView().getVisibility());

        testScene.beginTransaction();
        testScene.show(childScene);
        testScene.commitTransaction();

        assertEquals(State.ACTIVITY_CREATED, childScene.getState());
        assertEquals(View.VISIBLE, childScene.getView().getVisibility());
        assertTrue(testScene.isShow(childScene));
    }
}
