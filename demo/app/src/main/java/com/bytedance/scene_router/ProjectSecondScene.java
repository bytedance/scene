package com.bytedance.scene_router;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.bytedance.scene.Scene;
import com.bytedance.scenerouter.annotation.RouterValue;
import com.bytedance.scenerouter.annotation.SceneUrl;
import com.bytedance.scenerouter.core.SceneRouter;

import java.io.Serializable;
import java.util.ArrayList;

@SceneUrl("/root/1")
public class ProjectSecondScene extends Scene {
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireSceneContext());
    }
}
