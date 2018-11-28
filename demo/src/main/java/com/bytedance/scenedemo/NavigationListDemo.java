package com.bytedance.scenedemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scenedemo.multi_stack.MultiStackDemoScene;
import com.bytedance.scenedemo.navigation.configuration.ConfigurationDemoScene;
import com.bytedance.scenedemo.navigation.forresult.SceneResultRootScene;
import com.bytedance.scenedemo.navigation.performance.PerformanceDemo;
import com.bytedance.scenedemo.navigation.popinterupt.PopInteruptScene_0;
import com.bytedance.scenedemo.navigation.popto.PopToScene;
import com.bytedance.scenedemo.navigation.push_clear_current.PushClearCurrentDemoScene;
import com.bytedance.scenedemo.navigation.push_pop.PushPopDemoScene;
import com.bytedance.scenedemo.navigation.push_singletop.PushSingleTopRootScene;
import com.bytedance.scenedemo.navigation.pushandclear.PushClearRootScene;
import com.bytedance.scenedemo.navigation.remove.RemoveDemoScene;
import com.bytedance.scenedemo.navigation.reuse.ReuseDemoScene;
import com.bytedance.scenedemo.navigation.singletask.SingleTaskDemoScene;
import com.bytedance.scenedemo.navigation.softkeyboard.SoftkeyboardDemoScene;
import com.bytedance.scenedemo.navigation.window.WindowDemo;
import com.bytedance.scenedemo.template.DefaultScene;
import com.bytedance.scenedemo.theme.ThemeDemo;

/**
 * Created by JiangQi on 8/21/18.
 */
public class NavigationListDemo extends UserVisibleHintGroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(getActivity());

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        scrollView.addView(layout);

        Button button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Push Pop 用法");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PushPopDemoScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Push singleTop 用法");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PushSingleTopRootScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Push singleTask 用法");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(SingleTaskDemoScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("移除中间任意的Scene");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(RemoveDemoScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Push 清空Task");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PushClearRootScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Push 替换最前面的Scene");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PushClearCurrentDemoScene.class);
            }
        });

        View emptyView = new View(getActivity());
        layout.addView(emptyView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("导航 PopTo 用法");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PopToScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("导航 Pop拦截 用法");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PopInteruptScene_0.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Configuration变化 用法");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(ConfigurationDemoScene.class);
            }
        });

        emptyView = new View(getActivity());
        layout.addView(emptyView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("导航 拿返回值，权限");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(SceneResultRootScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("导航 复用，加快打开页面速度");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(ReuseDemoScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("导航 修改Activity属性后返回重置属性（未完成）");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(WindowDemo.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("导航 输入法问题");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(SoftkeyboardDemoScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("跟Activity比较性能");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PerformanceDemo.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Router（未完成）");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Deep Link（未完成）");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("模板 AppCompatScene");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(DefaultScene.class);
            }
        });


        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("MutliStack");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(MultiStackDemoScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("主题");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(ThemeDemo.class);
            }
        });

        emptyView = new View(getActivity());
        layout.addView(emptyView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800));

        return scrollView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible() && isVisibleToUser) {

        }
    }
}
