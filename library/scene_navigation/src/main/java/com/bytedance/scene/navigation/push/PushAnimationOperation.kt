/*
 * Copyright (C) 2019 ByteDance Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bytedance.scene.navigation.push

import com.bytedance.scene.State
import com.bytedance.scene.animation.AnimationInfo
import com.bytedance.scene.animation.NavigationAnimationExecutor
import com.bytedance.scene.navigation.NavigationManagerAbility
import com.bytedance.scene.navigation.NavigationScene
import com.bytedance.scene.navigation.Operation
import com.bytedance.scene.navigation.Record
import com.bytedance.scene.utlity.AnimatorUtility
import com.bytedance.scene.utlity.CancellationSignalList

/**
 * Created by jiangqi on 2025/4/9
 *
 * @author jiangqi@bytedance.com
 */
internal class PushAnimationOperation(private val managerAbility: NavigationManagerAbility, private val previousRecord: Record?) : Operation {
    private val navigationScene: NavigationScene = managerAbility.navigationScene
    private val previousSceneView = previousRecord?.mScene?.view

    override fun execute(operationEndAction: Runnable) {
        val currentRecordList = managerAbility.currentRecordList
        val topRecord = currentRecordList[currentRecordList.size - 1]

        if (previousRecord == null) {
            operationEndAction.run()
            return
        }

        //Navigation animation only execute when NavigationScene is visible, otherwise skip
        val isNavigationSceneInAnimationState = navigationScene.state.value >= State.STARTED.value

        if (managerAbility.isDisableNavigationAnimation || !isNavigationSceneInAnimationState || previousSceneView == null) {
            operationEndAction.run()
            return
        }

        var navigationAnimationExecutor: NavigationAnimationExecutor? = null
        //Scene can override mNavigationAnimationExecutor in moveState method by NavigationScene.overrideNavigationAnimationExecutor
        val recordAnimationExecutor = topRecord.mNavigationAnimationExecutor
        if (recordAnimationExecutor != null && recordAnimationExecutor.isSupport(previousRecord.mScene.javaClass, topRecord.mScene.javaClass)) {
            navigationAnimationExecutor = recordAnimationExecutor
        }
        if (navigationAnimationExecutor == null) {
            navigationAnimationExecutor = navigationScene.defaultNavigationAnimationExecutor
        }

        if (navigationAnimationExecutor != null && navigationAnimationExecutor.isSupport(previousRecord.mScene.javaClass, topRecord.mScene.javaClass)) {
            val finalCurrentScene = previousRecord.mScene

            AnimatorUtility.bringSceneViewToFrontIfNeeded(navigationScene) //make sure Z order is correct
            navigationAnimationExecutor.setAnimationViewGroup(navigationScene.animationContainer)

            val fromInfo = AnimationInfo(finalCurrentScene, previousSceneView, finalCurrentScene.state, previousRecord.mIsTranslucent)
            val toInfo = AnimationInfo(topRecord.mScene, topRecord.mScene.view, topRecord.mScene.state, topRecord.mIsTranslucent)

            val cancellationSignalList = CancellationSignalList()
            managerAbility.cancellationSignalManager.add(cancellationSignalList)

            navigationAnimationExecutor.executePushChange(
                navigationScene, navigationScene.view.rootView, fromInfo, toInfo, cancellationSignalList,
                Runnable {
                    managerAbility.cancellationSignalManager.remove(cancellationSignalList)
                    managerAbility.notifyNavigationAnimationEnd(finalCurrentScene, topRecord.mScene, true)
                    operationEndAction.run()
                })
        } else {
            operationEndAction.run()
        }
    }
}