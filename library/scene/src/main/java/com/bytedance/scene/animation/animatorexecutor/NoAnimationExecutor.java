package com.bytedance.scene.animation.animatorexecutor;

import android.support.annotation.NonNull;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.utlity.CancellationSignal;

/**
 * Created by JiangQi on 8/22/18.
 */
public class NoAnimationExecutor extends NavigationAnimationExecutor {
    @Override
    public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
        return true;
    }

    @Override
    public void executePushChangeCancelable(@NonNull AnimationInfo fromInfo, @NonNull AnimationInfo toInfo, @NonNull Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
        endAction.run();
    }

    @Override
    public void executePopChangeCancelable(@NonNull AnimationInfo fromInfo, @NonNull AnimationInfo toInfo, @NonNull Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
        endAction.run();
    }
}
