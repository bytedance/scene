package com.bytedance.scene.navigation.idle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.post.NavigationSourceSupportPostUtility;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by sunyongsheng.aengus on 2024/12/9
 * @author sunyongsheng.aengus@bytedance.com
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class PopUseIdleWhenStopTests {

    @Test
    public void testPushAndPopImmediateNoException() {
        ChildScene rootScene = new ChildScene();
        NavigationScene navigationScene = NavigationSourceSupportPostUtility.createFromSceneLifecycleManager(rootScene);
        ChildScene childScene = new ChildScene();
        navigationScene.push(childScene);

        PopOptions popOptions = new PopOptions.Builder().setUsePost(true).setUseIdleWhenStop(true).build();
        navigationScene.pop(popOptions);
        navigationScene.forceExecutePendingNavigationOperation();

        Assert.assertSame(navigationScene.getCurrentScene(), rootScene);
        Assert.assertEquals(childScene.getState(), State.NONE);
    }

    class ChildScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }
}