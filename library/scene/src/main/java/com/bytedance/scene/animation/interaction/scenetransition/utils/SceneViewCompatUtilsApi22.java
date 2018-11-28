package com.bytedance.scene.animation.interaction.scenetransition.utils;

import android.annotation.TargetApi;
import android.view.View;

/**
 * Created by JiangQi on 10/23/18.
 */
@TargetApi(22)
class SceneViewCompatUtilsApi22 extends SceneViewCompatUtilsApi21 {
    private LeftTopRightBottomRefUtils mLeftTopRightBottomRefUtils = new LeftTopRightBottomRefUtils();

    @Override
    public void setLeftTopRightBottom(View v, int left, int top, int right, int bottom) {
        mLeftTopRightBottomRefUtils.setLeftTopRightBottom(v, left, top, right, bottom);
    }
}
