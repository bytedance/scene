package com.bytedance.scene;

/**
 * Created by fengminchao on 2023/10/18
 *
 * @author fengminchao@bytedance.com
 */
public class SceneGlobalConfig {
    public static volatile boolean validateScopeAndViewModelStoreSceneClassStrategy = false;
    public static volatile boolean sceneLifecycleCallbackObjectCreationOpt = false;
    public static volatile boolean shouldSkipDispatchWindowFocusChangeToNotReadyScene = false;
    public static volatile boolean usePreviousSavedStateWhenPauseIfPossible = false;
    public static volatile boolean cancelAnimationWhenForceExecutePendingNavigationOperation = false;
    public static volatile boolean fixNavigationMessageQueueMessageOrderIssue = false;
}