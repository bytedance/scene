package com.bytedance.scene;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.ViewIdGenerator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SceneSuperNotCalledExceptionTests {
    @Test(expected = SuperNotCalledException.class)
    public void testSuperNotCalledException_Attach() {
        NavigationSourceUtility.createFromSceneLifecycleManager(new Scene() {
            @Override
            public void onAttach() {
                //throw exception
            }

            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        });
    }

    @Test(expected = SuperNotCalledException.class)
    public void testSuperNotCalledException_Create() {
        NavigationSourceUtility.createFromSceneLifecycleManager(new Scene() {
            @Override
            public void onCreate(@Nullable Bundle savedInstanceState) {
                //throw exception
            }

            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        });
    }

    @Test(expected = SuperNotCalledException.class)
    public void testSuperNotCalledException_ActivityCreated() {
        NavigationSourceUtility.createFromSceneLifecycleManager(new Scene() {
            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                //throw exception
            }

            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        });
    }

    @Test(expected = SuperNotCalledException.class)
    public void testSuperNotCalledException_ViewCreated() {
        NavigationSourceUtility.createFromSceneLifecycleManager(new Scene() {
            @Override
            public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                //throw exception
            }

            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        });
    }

    @Test(expected = SuperNotCalledException.class)
    public void testSuperNotCalledException_Start() {
        SceneLifecycleManager manager = NavigationSourceUtility.createFromInitSceneLifecycleManager(new Scene() {
            @Override
            public void onStart() {
                //throw exception
            }

            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        }).first;
        manager.onStart();
    }

    @Test(expected = SuperNotCalledException.class)
    public void testSuperNotCalledException_Resume() {
        SceneLifecycleManager manager = NavigationSourceUtility.createFromInitSceneLifecycleManager(new Scene() {
            @Override
            public void onResume() {
                //throw exception
            }

            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        }).first;
        manager.onStart();
        manager.onResume();
    }

    @Test(expected = SuperNotCalledException.class)
    public void testSuperNotCalledException_DestroyView() {
        SceneLifecycleManager manager = NavigationSourceUtility.createFromInitSceneLifecycleManager(new Scene() {
            @Override
            public void onDestroyView() {
                //throw exception
            }

            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        }).first;
        manager.onStart();
        manager.onResume();
        manager.onPause();
        manager.onStop();
        manager.onDestroyView();
    }

    @Test(expected = SuperNotCalledException.class)
    public void testSuperNotCalledException_Detach() {
        SceneLifecycleManager manager = NavigationSourceUtility.createFromInitSceneLifecycleManager(new Scene() {
            @Override
            public void onDetach() {
                //throw exception
            }

            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        }).first;
        manager.onStart();
        manager.onResume();
        manager.onPause();
        manager.onStop();
        manager.onDestroyView();
    }

    @Test(expected = SuperNotCalledException.class)
    public void testSuperNotCalledException_Destroy() {
        SceneLifecycleManager manager = NavigationSourceUtility.createFromInitSceneLifecycleManager(new Scene() {
            @Override
            public void onDestroy() {
                //throw exception
            }

            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        }).first;
        manager.onStart();
        manager.onResume();
        manager.onPause();
        manager.onStop();
        manager.onDestroyView();
    }

    @Test(expected = SuperNotCalledException.class)
    public void testSuperNotCalledException_Pause() {
        SceneLifecycleManager manager = NavigationSourceUtility.createFromInitSceneLifecycleManager(new Scene() {
            @Override
            public void onPause() {
                //throw exception
            }

            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        }).first;
        manager.onStart();
        manager.onResume();
        manager.onPause();
    }

    @Test(expected = SuperNotCalledException.class)
    public void testSuperNotCalledException_Stop() {
        SceneLifecycleManager manager = NavigationSourceUtility.createFromInitSceneLifecycleManager(new Scene() {
            @Override
            public void onStop() {
                //throw exception
            }

            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        }).first;
        manager.onStart();
        manager.onResume();
        manager.onPause();
        manager.onStop();
    }

    @Test(expected = SuperNotCalledException.class)
    public void testSuperNotCalledException_SaveInstanceState() {
        final Scene scene = new NotCallSuperScene();
        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();

        TestGroupScene testGroupScene = new TestGroupScene();
        testGroupScene.add(testGroupScene.mId, scene, "tag");

        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };
        SceneLifecycleManager<GroupScene> manager = new SceneLifecycleManager<>();
        manager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                testGroupScene, rootScopeFactory, true, null);
        manager.onStart();
        manager.onResume();
        manager.onSaveInstanceState(new Bundle());
    }

    public static class NotCallSuperScene extends Scene{
        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            //throw exception
        }

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    public static class TestChildScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    public static class TestGroupScene extends GroupScene {
        public final int mId;

        public TestGroupScene() {
            mId = ViewIdGenerator.generateViewId();
        }

        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            FrameLayout layout = new FrameLayout(requireSceneContext());
            layout.setId(mId);
            return layout;
        }
    }
}
