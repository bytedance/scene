package com.bytedance.scene;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.group.GroupScene;
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
        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();

        GroupScene groupScene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }
        };

        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };

        SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                groupScene, rootScopeFactory, false, null);
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
        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();

        GroupScene groupScene=new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }
        };

        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };

        SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                groupScene, rootScopeFactory, false, null);
        sceneLifecycleManager.onDestroyView();
    }

    @Test(expected = NullPointerException.class)
    public void testNPE() {
        SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        sceneLifecycleManager.onActivityCreated(null, null,
                null, null,
                false, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNPE1() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        sceneLifecycleManager.onActivityCreated(testActivity, null,
                null, null,
                false, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNPE2() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                null, null,
                false, null);

    }

    @Test(expected = NullPointerException.class)
    public void testNPE3() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        GroupScene groupScene=new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }
        };

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                groupScene, null, false, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnSaveInstanceStateException() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        GroupScene groupScene=new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }
        };

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                groupScene, new Scope.RootScopeFactory() {
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
        SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        GroupScene groupScene=new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }
        };

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                groupScene, new Scope.RootScopeFactory() {
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
        SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        GroupScene groupScene=new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }
        };

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                groupScene, new Scope.RootScopeFactory() {
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
        SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        GroupScene groupScene=new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }
        };

        groupScene.dispatchAttachActivity(testActivity);
        groupScene.dispatchAttachScene(null);
        groupScene.dispatchCreate(null);
        groupScene.dispatchCreateView(null, new FrameLayout(testActivity));

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                groupScene, new Scope.RootScopeFactory() {
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
        SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        GroupScene groupScene=new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }
        };

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                groupScene, new Scope.RootScopeFactory() {
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
        SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        GroupScene groupScene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }
        };

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                groupScene, new Scope.RootScopeFactory() {
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
        SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        GroupScene groupScene=new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }
        };

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                groupScene, new Scope.RootScopeFactory() {
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
        SceneLifecycleManager sceneLifecycleManager = new SceneLifecycleManager();
        sceneLifecycleManager.onStart();
    }

    @Test(expected = IllegalStateException.class)
    public void testStateException5() {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();

        GroupScene groupScene=new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }
        };

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                groupScene, new Scope.RootScopeFactory() {
                    @NonNull
                    @Override
                    public Scope getRootScope() {
                        return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                    }
                }, false, null);
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                groupScene, new Scope.RootScopeFactory() {
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
