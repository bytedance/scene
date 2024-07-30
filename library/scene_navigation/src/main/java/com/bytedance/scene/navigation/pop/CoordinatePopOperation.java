package com.bytedance.scene.navigation.pop;

import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.queue.NavigationMessageQueue;

/**
 * Created by jiangqi on 2023/11/15
 *
 * @author jiangqi@bytedance.com
 */
public class CoordinatePopOperation implements Operation {
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationMessageQueue mMessageQueue;
    private final NavigationAnimationExecutor mAnimationFactory;
    private final PopOptions mPopOptions;

    public CoordinatePopOperation(NavigationManagerAbility navigationManagerAbility, NavigationMessageQueue messageQueue,
                                  NavigationAnimationExecutor animationFactory, PopOptions popOptions) {
        this.mManagerAbility = navigationManagerAbility;
        this.mMessageQueue = messageQueue;
        this.mPopOptions = popOptions;
        this.mAnimationFactory = animationFactory;
    }

    @Override
    public void execute(Runnable operationEndAction) {
        new CoordinatePopCountOperation(mManagerAbility, mMessageQueue, mAnimationFactory, 1, mPopOptions).execute(operationEndAction);
    }
}
