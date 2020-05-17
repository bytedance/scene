package org.qiibeta.testscenerouter;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.bytedance.scene.ui.template.AppCompatScene;
import com.bytedance.scenerouter.annotation.SceneUrl;
import com.bytedance.scenerouter.core.OpenCallback;
import com.bytedance.scenerouter.core.SceneNotFoundException;
import com.bytedance.scenerouter.core.SceneRouter;
import com.bytedance.scenerouter.core.SceneRouters;

@SceneUrl("/test")
public class MainScene extends AppCompatScene {
    private Button mButton;

    @NonNull
    @Override
    public View onCreateContentView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout layout = new FrameLayout(requireActivity());
        mButton = new Button(requireActivity());
        mButton.setAllCaps(false);
        layout.addView(mButton, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getToolbar().setNavigationIcon(null);
        setTitle("Main");

        final String targetUrl = "/test1";
        mButton.setText("click to open " + targetUrl);
        mButton.setOnClickListener(new View.OnClickListener() {
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
                sceneRouter.url(targetUrl).argument("haha", "success").open(new OpenCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFail(@Nullable Exception exception) {
                        if (exception instanceof SceneNotFoundException) {
                            Toast.makeText(requireActivity(), "not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
