package com.bytedance.scene;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bytedance.scene.utlity.SceneInternalException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SceneExceptionTests {
    @Test
    public void testStateException() {
        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric
                .buildActivity(NavigationSourceUtility.TestActivity.class)
                .create()
                .start()
                .resume();
        final NavigationSourceUtility.TestActivity testActivity = controller.get();

        Assert.assertThrows(SceneInternalException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                Scene scene = new Scene() {
                    @NonNull
                    @Override
                    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                        return new View(requireSceneContext());
                    }
                };
                scene.setSeparateCreateFromCreateView(true);
                scene.dispatchCreate(null);
                scene.dispatchStop();
            }
        });

        Assert.assertThrows(SceneInternalException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                Scene scene = new Scene() {
                    @NonNull
                    @Override
                    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                        return new View(requireSceneContext());
                    }
                };
                scene.setSeparateCreateFromCreateView(true);
                scene.dispatchAttachActivity(testActivity);
                scene.dispatchCreate(null);
                scene.dispatchCreateView(null, testActivity.mFrameLayout);
                scene.dispatchStart();
                scene.dispatchResume();
                scene.dispatchStop();
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullViewException() {
        NavigationSourceUtility.createFromSceneLifecycleManager(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return null;
            }
        });
    }

    @Test(expected = IllegalStateException.class)
    public void testSetThemeAfterViewCreatedException() {
        NavigationSourceUtility.createFromSceneLifecycleManager(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onResume() {
                super.onResume();
                setTheme(android.R.style.DeviceDefault_ButtonBar);
            }
        });
    }

    @Test
    public void testSetTheme() {
        NavigationSourceUtility.createFromSceneLifecycleManager(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                setTheme(android.R.style.DeviceDefault_ButtonBar);
                return new View(requireSceneContext());
            }

            @Override
            public void onResume() {
                super.onResume();
                assertEquals(android.R.style.DeviceDefault_ButtonBar, getTheme());
            }
        });
    }
}
