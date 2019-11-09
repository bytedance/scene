package com.bytedance.scene;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
@LooperMode(PAUSED)
public class SceneLifecycleManagerExceptionTests {
    @Test
    public void testTranslucentActivity() {
        final Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();
        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(scene.getClass());
        navigationScene.setArguments(options.toBundle());

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

        navigationScene.setDefaultNavigationAnimationExecutor(null);
        navigationScene.setRootSceneComponentFactory(sceneComponentFactory);
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, rootScopeFactory, false, null);
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onDestroyView();
    }

    @Test
    public void testSkipOnStartOnResumeOnPauseOnStop() {
        final Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();
        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(scene.getClass());
        navigationScene.setArguments(options.toBundle());

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

        navigationScene.setDefaultNavigationAnimationExecutor(null);
        navigationScene.setRootSceneComponentFactory(sceneComponentFactory);
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, rootScopeFactory, false, null);
        sceneLifecycleManager.onDestroyView();
    }

    @Test(expected = NullPointerException.class)
    public void testNPE() {
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        sceneLifecycleManager.onActivityCreated(null, null,
                null, null,
                false, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNPE1() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        sceneLifecycleManager.onActivityCreated(testActivity, null,
                null, null,
                false, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNPE2() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                null, null,
                false, null);

    }

    @Test(expected = NullPointerException.class)
    public void testNPE3() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(ChildScene.class);
        navigationScene.setArguments(options.toBundle());

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, null, false, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnSaveInstanceStateException() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(ChildScene.class);
        navigationScene.setArguments(options.toBundle());

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, new Scope.RootScopeFactory() {
                    @NonNull
                    @Override
                    public Scope getRootScope() {
                        return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                    }
                }, false, new Bundle());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnSaveInstanceStateException1() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(ChildScene.class);
        navigationScene.setArguments(options.toBundle());

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, new Scope.RootScopeFactory() {
                    @NonNull
                    @Override
                    public Scope getRootScope() {
                        return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                    }
                }, false, null);
        sceneLifecycleManager.onSaveInstanceState(new Bundle());
    }

    @Test(expected = NullPointerException.class)
    public void testOnSaveInstanceStateExceptionNPE() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(ChildScene.class);
        navigationScene.setArguments(options.toBundle());

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, new Scope.RootScopeFactory() {
                    @NonNull
                    @Override
                    public Scope getRootScope() {
                        return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                    }
                }, true, null);
        sceneLifecycleManager.onSaveInstanceState(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testNavigationSceneStateIncorrectExceptionNPE() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(ChildScene.class);
        navigationScene.setArguments(options.toBundle());

        navigationScene.dispatchAttachActivity(testActivity);
        navigationScene.dispatchAttachScene(null);
        navigationScene.dispatchCreate(null);
        navigationScene.dispatchCreateView(null, new FrameLayout(testActivity));

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, new Scope.RootScopeFactory() {
                    @NonNull
                    @Override
                    public Scope getRootScope() {
                        return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                    }
                }, false, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testStateException() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(ChildScene.class);
        navigationScene.setArguments(options.toBundle());

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, new Scope.RootScopeFactory() {
                    @NonNull
                    @Override
                    public Scope getRootScope() {
                        return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                    }
                }, false, null);
        sceneLifecycleManager.onResume();
    }

    @Test(expected = IllegalStateException.class)
    public void testStateException1() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(ChildScene.class);
        navigationScene.setArguments(options.toBundle());

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, new Scope.RootScopeFactory() {
                    @NonNull
                    @Override
                    public Scope getRootScope() {
                        return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                    }
                }, false, null);
        sceneLifecycleManager.onPause();
    }

    @Test(expected = IllegalStateException.class)
    public void testStateException2() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(ChildScene.class);
        navigationScene.setArguments(options.toBundle());

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, new Scope.RootScopeFactory() {
                    @NonNull
                    @Override
                    public Scope getRootScope() {
                        return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                    }
                }, false, null);
        sceneLifecycleManager.onStop();
    }

    @Test(expected = IllegalStateException.class)
    public void testStateException4() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager sceneLifecycleManager = new SceneLifecycleManager();

        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(ChildScene.class);
        navigationScene.setArguments(options.toBundle());
        sceneLifecycleManager.onStart();
    }

    @Test(expected = IllegalStateException.class)
    public void testStateException5() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(ChildScene.class);
        navigationScene.setArguments(options.toBundle());
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, new Scope.RootScopeFactory() {
                    @NonNull
                    @Override
                    public Scope getRootScope() {
                        return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                    }
                }, false, null);
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, new Scope.RootScopeFactory() {
                    @NonNull
                    @Override
                    public Scope getRootScope() {
                        return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                    }
                }, false, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testStateException6() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager sceneLifecycleManager = new SceneLifecycleManager();

        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(ChildScene.class);
        navigationScene.setArguments(options.toBundle());
        sceneLifecycleManager.onSaveInstanceState(new Bundle());
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

    public static class ChildScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }
}
