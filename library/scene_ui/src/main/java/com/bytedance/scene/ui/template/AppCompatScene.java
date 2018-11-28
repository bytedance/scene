package com.bytedance.scene.ui.template;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by JiangQi on 8/24/18.
 */
public abstract class AppCompatScene extends SwipeBackAppCompatScene {

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setSwipeEnabled(false);
    }
}

