package com.bytedance.scenedemo.group.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;
import com.bytedance.scene.LifeCycleFrameLayout;
import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.Scope;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scenedemo.MainListScene;

/**
 * Created by JiangQi on 11/6/18.
 */
public class TestSceneDelegateToViewView extends LifeCycleFrameLayout {
    public static final String SERVICE_NAME = "SERVICE";

    public TestSceneDelegateToViewView(@NonNull Context context) {
        super(context);
        init();
    }

    public TestSceneDelegateToViewView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestSceneDelegateToViewView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TestSceneDelegateToViewView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        NavigationSceneOptions options = new NavigationSceneOptions(MainListScene.class);
        options.setDrawWindowBackground(false);
        NavigationScene navigationScene = (NavigationScene) SceneInstanceUtility.getInstanceFromClass(NavigationScene.class,
                options.toBundle());
        setNavigationScene(navigationScene);
        setRootSceneComponentFactory(new SceneComponentFactory() {
            @Override
            public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                Toast.makeText(getContext(), "创建 " + className, Toast.LENGTH_SHORT).show();
                return null;
            }
        });
        setRootScopeFactory(new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                Scope scope = Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                scope.register(TestSceneDelegateToViewView.SERVICE_NAME, "Test");
                return scope;
            }
        });
    }

    @Override
    public boolean isSupportRestore() {
        return true;
    }

    @Override
    public void startActivityForResult(@NonNull Intent intent, int requestCode) {
        getActivity().startActivityForResult(intent, requestCode);
    }

    @Override
    public void requestPermissions(@NonNull String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActivity().requestPermissions(permissions, requestCode);
        }
    }

    private Activity getActivity() {
        Activity activity = null;
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                activity = (Activity) context;
                break;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return activity;
    }

    public static class TestScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = new View(getActivity());
            view.setBackgroundColor(Color.RED);
            return view;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            String value = getScope().getService(TestSceneDelegateToViewView.SERVICE_NAME);
            Toast.makeText(requireActivity(), value, Toast.LENGTH_SHORT).show();
        }
    }
}
