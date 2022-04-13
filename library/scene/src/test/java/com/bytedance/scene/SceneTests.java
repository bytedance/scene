package com.bytedance.scene;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleObserver;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.ViewIdGenerator;
import com.bytedance.scene.view.SceneContextThemeWrapper;
import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SceneTests {
    @Test
    public void testNonNullArguments() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        Bundle bundle = new Bundle();
        scene.setArguments(bundle);
        assertSame(bundle, scene.requireArguments());
        assertSame(bundle, scene.getArguments());
    }

    @Test(expected = IllegalStateException.class)
    public void testRequireArgumentsException() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        scene.requireArguments();
    }

    @Test
    public void testRequireViewAfterOnCreateView() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene);
        assertNotNull(scene.getView());
        assertNotNull(scene.requireView());
    }

    @Test(expected = IllegalStateException.class)
    public void testRequireViewExceptionBeforeOnCreateView() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        scene.requireView();
    }

    @Test(expected = IllegalStateException.class)
    public void testRequireParentSceneExceptionBeforeAttach() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        scene.requireParentScene();
    }

    @Test
    public void testRequireParentSceneAfterAttach() {
        GroupScene scene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }
        };
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene);

        Scene childScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };

        scene.requireView().setId(ViewIdGenerator.generateViewId());
        scene.add(scene.requireView().getId(), childScene, "TAG");
        assertNotNull(childScene.getParentScene());
        assertNotNull(childScene.requireParentScene());
        assertSame(scene, childScene.requireParentScene());
    }

    @Test(expected = IllegalStateException.class)
    public void testRootSceneRequireParentSceneException() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene);

        assertNull(scene.getParentScene().getParentScene());
        scene.getParentScene().requireParentScene();
    }

    @Test(expected = IllegalStateException.class)
    public void testRequireActivityBeforeOnAttach() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        scene.requireActivity();
    }

    @Test(expected = IllegalStateException.class)
    public void testRequireSceneContextBeforeOnAttach() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        scene.requireSceneContext();
    }

    @Test(expected = IllegalStateException.class)
    public void testRequireApplicationContextBeforeOnAttach() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        scene.requireApplicationContext();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetLayoutInflaterBeforeOnAttach() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        scene.getLayoutInflater();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetScopeBeforeOnCreate() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        scene.getScope();
    }

    @Test
    public void testExecuteNowOrScheduleAtNextResume() {
        final AtomicBoolean mValue = new AtomicBoolean(false);
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                executeNowOrScheduleAtNextResume(new Runnable() {
                    @Override
                    public void run() {
                        mValue.set(true);
                    }
                });
                assertFalse(mValue.get());
            }

            @Override
            public void onResume() {
                super.onResume();
                assertTrue(mValue.get());
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(scene);
        assertTrue(mValue.get());

        final AtomicBoolean second = new AtomicBoolean();
        scene.executeNowOrScheduleAtNextResume(new Runnable() {
            @Override
            public void run() {
                second.set(true);
            }
        });
        assertTrue(second.get());
    }

    @Test
    public void test_LAYOUT_INFLATER_SERVICE() {
        final AtomicReference<View> reference = new AtomicReference<>();
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);
                reference.set(getView());
            }
        };
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene);
        SceneLifecycleManager manager = pair.first;
        manager.onStart();
        manager.onResume();
        assertNotNull(reference.get().getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    @Test
    public void test_LAYOUT_INFLATER_SERVICE_WhenThemeSet() {
        final AtomicReference<View> reference = new AtomicReference<>();
        Scene scene = new Scene() {
            @Override
            public void onAttach() {
                super.onAttach();
                setTheme(android.R.style.Theme);
            }

            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);
                reference.set(getView());
            }
        };
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene);
        SceneLifecycleManager manager = pair.first;
        manager.onStart();
        manager.onResume();
        assertNotNull(reference.get().getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    @Test
    public void test_LAYOUT_INFLATER_SERVICE_afterOnDestroyView() {
        final AtomicReference<View> reference = new AtomicReference<>();
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);
                reference.set(getView());
            }
        };
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene);
        SceneLifecycleManager manager = pair.first;
        manager.onStart();
        manager.onResume();
        manager.onPause();
        manager.onStop();
        manager.onDestroyView();
        assertNotNull(reference.get().getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    @Test
    public void test_LAYOUT_INFLATER_SERVICE_afterOnDestroyViewWhenThemeSet() {
        final AtomicReference<View> reference = new AtomicReference<>();
        Scene scene = new Scene() {
            @Override
            public void onAttach() {
                super.onAttach();
                setTheme(android.R.style.Theme);
            }

            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);
                reference.set(getView());
            }
        };
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene);
        SceneLifecycleManager manager = pair.first;
        manager.onStart();
        manager.onResume();
        manager.onPause();
        manager.onStop();
        manager.onDestroyView();
        assertNotNull(reference.get().getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    @Test
    public void testFindViewById() {
        final int id = ViewIdGenerator.generateViewId();
        final AtomicReference<View> reference = new AtomicReference<>();
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                View view = new View(requireSceneContext());
                view.setId(id);
                reference.set(view);
                return view;
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(scene);
        assertNotNull(scene.findViewById(id));
        assertSame(reference.get(), scene.findViewById(id));
        assertNull(scene.findViewById(ViewIdGenerator.generateViewId()));
    }

    @Test
    public void testFindViewByIdBeforeOnCreateView() {
        final int id = ViewIdGenerator.generateViewId();
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                View view = new View(requireSceneContext());
                view.setId(id);
                return view;
            }
        };
        assertNull(scene.findViewById(id));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequireViewById() {
        final int id = ViewIdGenerator.generateViewId();
        final AtomicReference<View> reference = new AtomicReference<>();
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                View view = new View(requireSceneContext());
                reference.set(view);
                return view;
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(scene);
        scene.requireViewById(id);
    }

    @Test
    public void testGetString() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(scene);
        assertNotNull(scene.getString(android.R.string.cancel));
    }

    @Test
    public void testGetStringFormatArgs() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(scene);
        assertNotNull(scene.getString(TestResources.getString(scene, "ut_get_string"), "Value"));
        assertEquals("UnitTestGetStringValue", scene.getString(TestResources.getString(scene, "ut_get_string"), "Value"));
    }


    @Test
    public void testGetText() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(scene);
        assertNotNull(scene.getText(android.R.string.cancel));
    }

    @Test
    public void testFixSceneReuseLifecycleAdapterReset() {
        final int id = ViewIdGenerator.generateViewId();
        GroupScene groupScene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                FrameLayout layout = new FrameLayout(requireSceneContext());
                layout.setId(id);
                return layout;
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(groupScene);
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        scene.getLifecycle().addObserver(new LifecycleObserver() {

        });
        groupScene.add(id, scene, "TAG");
        groupScene.remove(scene);
        groupScene.add(id, scene, "TAG");
    }

    @Test
    public void testViewContext() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireActivity());
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(scene);
        assertSame(scene.requireActivity(), scene.getView().getContext());

        Scene scene2 = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(new SceneContextThemeWrapper(requireActivity(), requireActivity().getTheme()));
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(scene2);

        Scene scene3 = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(new SceneContextThemeWrapper(requireSceneContext(), requireSceneContext().getTheme()) {
                    //subclass of ContextWrapper
                });
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(scene3);

        Scene scene4 = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireApplicationContext());
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(scene4);
    }

    @Test
    public void testViewSceneContext() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(scene);
        assertSame(scene.requireSceneContext(), scene.getView().getContext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testViewContextThemeException() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                setTheme(android.R.style.Theme_Light);
                return new View(requireActivity());
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(scene);
    }

    @Test
    public void testGetDebugSceneHierarchy() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        GroupScene rootGroupScene = NavigationSourceUtility.createFromSceneLifecycleManager(scene);
        Truth.assertThat(rootGroupScene.getDebugSceneHierarchy()).contains(scene.getClass().getSimpleName());
    }
}
