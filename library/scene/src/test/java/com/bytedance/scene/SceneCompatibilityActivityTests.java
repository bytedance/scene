package com.bytedance.scene;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;
import com.bytedance.scene.interfaces.ActivityResultCallback;
import com.bytedance.scene.interfaces.PermissionResultCallback;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SceneCompatibilityActivityTests {
    @Test
    public void testStartActivityForResult() {
        final Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        final NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(scene.getClass());
        navigationScene.setArguments(options.toBundle());

        final Intent[] intents = new Intent[1];
        final int[] requestCodes = new int[1];

        NavigationScene.NavigationSceneHost navigationSceneHost = new NavigationScene.NavigationSceneHost() {
            @Override
            public boolean isSupportRestore() {
                return false;
            }

            @Override
            public void startActivityForResult(@NonNull Intent intent, int requestCode) {
                intents[0] = intent;
                requestCodes[0] = requestCode;

                Intent result = new Intent();
                result.putExtra("result", true);
                navigationScene.onActivityResult(requestCode, Activity.RESULT_OK, result);
            }

            @Override
            public void requestPermissions(@NonNull String[] permissions, int requestCode) {

            }
        };

        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };

        SceneComponentFactory sceneComponentFactory = new SceneComponentFactory() {
            @Override
            public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                if (className.equals(scene.getClass().getName())) {
                    return scene;
                }
                return null;
            }
        };

        navigationScene.setDefaultNavigationAnimationExecutor(new NoAnimationExecutor());

        SceneLifecycleManager sceneLifecycleManager = new SceneLifecycleManager();
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, navigationSceneHost, rootScopeFactory,
                sceneComponentFactory, null);
        Intent sendIntent = new Intent();
        final boolean[] resultArray = new boolean[1];
        scene.getNavigationScene().startActivityForResult(sendIntent, 1234, new ActivityResultCallback() {
            @Override
            public void onResult(int resultCode, @Nullable Intent result) {
                assertTrue(result.getBooleanExtra("result", false));
                resultArray[0] = true;
            }
        });
        assertTrue(resultArray[0]);
    }

    @Test
    public void testPermission() {
        final Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        final NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(scene.getClass());
        navigationScene.setArguments(options.toBundle());

        NavigationScene.NavigationSceneHost navigationSceneHost = new NavigationScene.NavigationSceneHost() {
            @Override
            public boolean isSupportRestore() {
                return false;
            }

            @Override
            public void startActivityForResult(@NonNull Intent intent, int requestCode) {

            }

            @Override
            public void requestPermissions(@NonNull String[] permissions, int requestCode) {
                navigationScene.onRequestPermissionsResult(requestCode, permissions, new int[]{android.content.pm.PackageManager.PERMISSION_GRANTED});
            }
        };

        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };

        SceneComponentFactory sceneComponentFactory = new SceneComponentFactory() {
            @Override
            public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                if (className.equals(scene.getClass().getName())) {
                    return scene;
                }
                return null;
            }
        };

        navigationScene.setDefaultNavigationAnimationExecutor(new NoAnimationExecutor());

        SceneLifecycleManager sceneLifecycleManager = new SceneLifecycleManager();
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, navigationSceneHost, rootScopeFactory,
                sceneComponentFactory, null);
        final boolean[] resultArray = new boolean[1];
        scene.getNavigationScene().requestPermissions(new String[]{}, 1234, new PermissionResultCallback() {
            @Override
            public void onResult(@Nullable int[] grantResults) {
                assertEquals(grantResults[0], PackageManager.PERMISSION_GRANTED);
                resultArray[0] = true;
            }
        });
        assertTrue(resultArray[0]);
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
