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
package com.bytedance.scene.navigation;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.interfaces.Function;
import com.bytedance.scene.navigation.reuse.IReuseScene;
import com.bytedance.scene.utlity.NavigationSceneViewUtility;
import com.bytedance.scene.utlity.SceneInternalException;
import com.bytedance.scene.utlity.Utility;

/**
 * Created by JiangQi on 8/4/25.
 */
public class SceneLifecycleStateScheduler {
    public static void transition(@NonNull NavigationScene navigationScene, @NonNull Scene scene, @NonNull State to, @Nullable Bundle bundle, boolean causedByActivityLifeCycle, @Nullable Function<Scene, Void> afterOnActivityCreatedAction, @Nullable Runnable endAction) {
        State currentState = scene.getState();
        if (currentState == to) {
            if (endAction != null) {
                endAction.run();
            }
            return;
        }

        if (currentState.value < to.value) {
            switch (currentState) {
                case NONE:
                    scene.dispatchAttachActivity(navigationScene.requireActivity());
                    scene.dispatchAttachScene(navigationScene);
                    scene.dispatchCreate(bundle);
                    transition(navigationScene, scene, to, bundle, causedByActivityLifeCycle, afterOnActivityCreatedAction, endAction);
                    break;
                case CREATED:
                    ViewGroup containerView = navigationScene.getSceneContainer();
                    scene.dispatchCreateView(bundle, containerView);
                    if (ActivityCompatibleInfoCollector.isTargetSceneType(scene)) {
                        Record record = navigationScene.findRecordByScene(scene);
                        navigationScene.mNavigationSceneManager.saveActivityCompatibleInfo(record);
                    }
                    if (!causedByActivityLifeCycle) {
                        if (scene.getView().getBackground() == null) {
                            Record record = navigationScene.findRecordByScene(scene);
                            if (!record.mIsTranslucent && navigationScene.mNavigationSceneOptions.fixSceneBackground()) {
                                int resId = navigationScene.mNavigationSceneOptions.getSceneBackgroundResId();
                                if (resId > 0) {
                                    scene.getView().setBackgroundDrawable(scene.requireSceneContext().getResources().getDrawable(resId));
                                } else {
                                    scene.getView().setBackgroundDrawable(Utility.getWindowBackground(scene.requireSceneContext()));
                                }
                                record.mSceneBackgroundSet = true;
                            }
                        }
                        /*
                         * TODO: What if the NavigationScene has been destroyed at this time?
                         * TODO: What to do with serialization
                         */
                        if (bundle != null) {
                            //Scene restore from save and restore path
                            int viewIndex = NavigationSceneViewUtility.targetViewIndexOfScene(navigationScene, navigationScene.mNavigationSceneOptions, scene);
                            containerView.addView(scene.getView(), viewIndex);
                        } else {
                            containerView.addView(scene.getView());
                        }
                    }
                    scene.getView().setVisibility(View.GONE);
                    transition(navigationScene, scene, to, bundle, causedByActivityLifeCycle, afterOnActivityCreatedAction, endAction);
                    break;
                case VIEW_CREATED:
                    scene.dispatchActivityCreated(bundle);
                    if (afterOnActivityCreatedAction != null) {
                        afterOnActivityCreatedAction.apply(scene);
                    }
                    transition(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                case ACTIVITY_CREATED:
                    scene.getView().setVisibility(View.VISIBLE);
                    if (navigationScene.isReusing(scene)) {
                        // The view may have been removed by NavigationAnimationExecutor in the reuse process,
                        // so we need to re-attach it to the view tree and prepare it for reuse if necessary.
                        doAttachWhenReuse(navigationScene, scene, bundle);
                    }
                    scene.dispatchStart();
                    transition(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                case STARTED:
                    scene.dispatchResume();
                    ((NavigationSceneManager) navigationScene.mNavigationSceneManager).onSceneResumedWindowFocusChanged(scene);
                    transition(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                default:
                    throw new SceneInternalException("unreachable state case " + currentState.getName());
            }
        } else {
            switch (currentState) {
                case RESUMED:
                    scene.dispatchPause();
                    ((NavigationSceneManager) navigationScene.mNavigationSceneManager).onScenePausedWindowFocusChanged(scene);
                    transition(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                case STARTED:
                    scene.dispatchStop();
                    if (!causedByActivityLifeCycle) {
                        scene.getView().setVisibility(View.GONE);
                    }
                    transition(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                case ACTIVITY_CREATED:
                    if (to == State.VIEW_CREATED) {
                        throw new IllegalArgumentException("cant switch state ACTIVITY_CREATED to VIEW_CREATED");
                    }
                    //continue
                case VIEW_CREATED:
                    ActivityCompatibleInfoCollector.clearHolder(scene);
                    View view = scene.getView();
                    scene.dispatchDestroyView();
                    if (!causedByActivityLifeCycle) {
                        Utility.removeFromParentView(view);
                    }
                    transition(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                case CREATED:
                    scene.dispatchDestroy();
                    scene.dispatchDetachScene();
                    scene.dispatchDetachActivity();
                    transition(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                default:
                    throw new SceneInternalException("unreachable state case " + currentState.getName());
            }
        }
    }

    /**
     * Ensures a reused scene's view is properly attached to the view hierarchy
     * and calls onPrepare only when the view needs to be re-attached.
     *
     * @param navigationScene The parent NavigationScene
     * @param scene           The Scene being reused
     * @param bundle          The saved state bundle, if any
     */
    static void doAttachWhenReuse(@NonNull NavigationScene navigationScene, @NonNull Scene scene, @Nullable Bundle bundle) {
        View sceneView = scene.getView();
        boolean isAttached = sceneView.isAttachedToWindow();
        boolean hasParent = sceneView.getParent() != null;
        ViewGroup container = navigationScene.getSceneContainer();

        if (!isAttached && !hasParent && (container != null)) {
            //The view is removed and needs to be added back to the container
            if (bundle != null) {
                int viewIndex = NavigationSceneViewUtility.targetViewIndexOfScene(navigationScene, navigationScene.mNavigationSceneOptions, scene);
                container.addView(sceneView, viewIndex);
            } else {
                container.addView(sceneView);
            }

            if (!(scene instanceof IReuseScene)) {
                throw new SceneInternalException("This Scene should implement IReuseScene");
            }
            ((IReuseScene) scene).onPrepare(bundle);
        }
    }
}
