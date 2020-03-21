package com.bytedance.scene;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.ViewIdGenerator;
import com.bytedance.scene.utlity.ViewUtility;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import static android.os.Looper.getMainLooper;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
@LooperMode(LooperMode.Mode.PAUSED)
public class SceneViewOnTouchEventTests {
    /**
     * test ViewRefUtility.cancelViewTouchTargetFromParent
     */
    @Test
    public void testCancelViewTouchTargetFromParent() {
        final int id = ViewIdGenerator.generateViewId();
        GroupScene groupScene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                getView().setId(id);
            }
        };
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(groupScene);

        TestScene scene = new TestScene();
        groupScene.add(id, scene, "TAG");

        shadowOf(getMainLooper()).idle();//execute Handler posted task

        View navSceneView = navigationScene.requireView();

        //force layout, otherwise View width and height will be zero
        navSceneView.measure(View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY));
        navSceneView.layout(0, 0, 100, 100);

        //send MotionEvent down event
        final long now = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, 0.0f, 0.0f, 0);
        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);

        navSceneView.dispatchTouchEvent(event);
        event.recycle();

        TestView testView = scene.getTestView();
        groupScene.remove(scene);

        assertTrue(testView.receivedCancelEvent);
    }

    private static class TestView extends View {
        private boolean receivedCancelEvent = false;

        public TestView(Context context) {
            super(context);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    //View should receive cancel event before Scene is destroyed
                    Scene scene = ViewUtility.findSceneByView(this);
                    assertNotNull(scene);
                    receivedCancelEvent = true;
                    return true;
            }
            return true;
        }
    }

    public static class TestScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new TestView(requireSceneContext());
        }

        private TestView getTestView() {
            return (TestView) requireView();
        }
    }
}
