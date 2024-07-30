package com.bytedance.scene.navigation.pop;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;
import com.bytedance.scene.queue.NavigationMessageQueue;
import com.bytedance.scene.utlity.Predicate;

import java.util.List;

/**
 * Created by jiangqi on 2023/11/15
 *
 * @author jiangqi@bytedance.com
 */
public class CoordinatePopOptionOperation implements Operation {
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationMessageQueue mMessageQueue;
    private final PopOptions mPopOptions;

    public CoordinatePopOptionOperation(NavigationManagerAbility navigationManagerAbility, NavigationMessageQueue messageQueue,
                                        PopOptions popOptions) {
        this.mManagerAbility = navigationManagerAbility;
        this.mMessageQueue = messageQueue;
        this.mPopOptions = popOptions;
    }

    @Override
    public void execute(Runnable operationEndAction) {
        List<Record> recordList = mManagerAbility.getCurrentRecordList();

        Predicate<Scene> popUtilPredicate = this.mPopOptions.getPopUtilPredicate();
        int count = 0;
        if (popUtilPredicate != null) {
            for (int i = recordList.size() - 1; i >= 0; i--) {
                Record record = recordList.get(i);
                if (popUtilPredicate.apply(record.mScene)) {
                    break;
                }
                count++;
            }
            new CoordinatePopCountOperation(mManagerAbility, mMessageQueue, mPopOptions.getNavigationAnimationExecutor(), count, mPopOptions).execute(operationEndAction);
        } else {
            new CoordinatePopOperation(mManagerAbility, mMessageQueue, mPopOptions.getNavigationAnimationExecutor(), mPopOptions).execute(operationEndAction);
        }
    }
}
