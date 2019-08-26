package com.bytedance.scene;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bytedance.scene.utlity.SceneInternalException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SceneExceptionTests {
    @Test(expected = SceneInternalException.class)
    public void testStateException() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        scene.dispatchStop();
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
