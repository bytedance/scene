package com.bytedance.scene;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.View;
import com.bytedance.scene.group.AsyncLayoutGroupScene;
import com.bytedance.scene.navigation.NavigationScene;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.util.concurrent.atomic.AtomicBoolean;

import static android.os.Looper.getMainLooper;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
@LooperMode(PAUSED)
public class AsyncLayoutGroupSceneTests {
    @Test
    public void test() {
        final AtomicBoolean onAsyncViewCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean onAsyncActivityCreatedCalled = new AtomicBoolean(false);
        final AtomicBoolean onAsyncStartCalled = new AtomicBoolean(false);
        final AtomicBoolean onAsyncResumeCalled = new AtomicBoolean(false);
        final AtomicBoolean onAsyncPauseCalled = new AtomicBoolean(false);
        final AtomicBoolean onAsyncStopCalled = new AtomicBoolean(false);

        AsyncLayoutGroupScene groupScene = new AsyncLayoutGroupScene() {
            @Override
            protected int getLayoutId() {
                return TestResources.getLayout(this, "view");
            }

            @Override
            public void onAsyncViewCreated(View view, @Nullable Bundle savedInstanceState) {
                super.onAsyncViewCreated(view, savedInstanceState);
                if (!onAsyncViewCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onAsyncActivityCreated(Bundle savedInstanceState) {
                super.onAsyncActivityCreated(savedInstanceState);
                if (!onAsyncActivityCreatedCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onAsyncStart() {
                super.onAsyncStart();
                if (!onAsyncStartCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onAsyncResume() {
                super.onAsyncResume();
                if (!onAsyncResumeCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onAsyncPause() {
                super.onAsyncPause();
                if (!onAsyncPauseCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }

            @Override
            public void onAsyncStop() {
                super.onAsyncStop();
                if (!onAsyncStopCalled.compareAndSet(false, true)) {
                    throw new IllegalStateException("crash");
                }
            }
        };
        groupScene.setAsyncLayoutEnabled(true);
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(groupScene);
        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        shadowOf(getMainLooper()).idle();//execute Handler posted task

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onDestroyView();
    }

    @Test
    public void isAsyncLayoutEnabled() {
        AsyncLayoutGroupScene groupScene = new AsyncLayoutGroupScene() {
            @Override
            protected int getLayoutId() {
                return TestResources.getLayout(this, "view");
            }
        };
        assertFalse(groupScene.isAsyncLayoutEnabled());
    }

    @Test
    public void testSetUserVisibleHint() {
        AsyncLayoutGroupScene groupScene = new AsyncLayoutGroupScene() {
            @Override
            protected int getLayoutId() {
                return TestResources.getLayout(this, "view");
            }
        };
        groupScene.setUserVisibleHint(true);
        assertFalse(groupScene.isAsyncLayoutEnabled());
        groupScene.setUserVisibleHint(false);
        assertTrue(groupScene.isAsyncLayoutEnabled());
    }
}
