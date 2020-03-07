package com.bytedance.scene_router;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.ui.template.AppCompatScene;
import com.bytedance.scenerouter.annotation.SceneUrl;
import com.bytedance.scenerouter.core.OpenCallback;
import com.bytedance.scenerouter.core.SceneNotFoundException;
import com.bytedance.scenerouter.core.SceneRouter;
import com.bytedance.scenerouter.core.SceneRouters;

@SceneUrl("/test")
public class MainScene extends AppCompatScene {
    @NonNull
    @Override
    public View onCreateContentView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireSceneContext());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SceneRouter sceneRouter = SceneRouters.of(MainScene.this);
//                sceneRouter.registerInterceptor(new Interceptor() {
//                    @Override
//                    public void process(@NonNull TaskInfo taskInfo, @NonNull ContinueTask continueTask) {
//                        String url = taskInfo.url;
//                        continueTask.onFail(null);
//                    }
//                });
//                sceneRouter.register();
                sceneRouter.url("/test1").argument("haha", "成功").open(new OpenCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFail(@Nullable Exception exception) {
                        if (exception instanceof SceneNotFoundException) {

                        }
                    }
                });
            }
        });
    }
}
