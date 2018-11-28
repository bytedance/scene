package com.bytedance.scene.group;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by JiangQi on 8/21/18.
 */
public abstract class InheritedScene extends GroupScene {
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getScope().register(getClass(), this);
    }
}