package com.bytedance.scene;

/**
 * Created by fengminchao on 2023/10/18
 *
 * @author fengminchao@bytedance.com
 */
public class SceneGlobalConfig {
    public static volatile int genScopeStrategy = 1;
    public static volatile boolean validateScopeAndViewModelStoreSceneClassStrategy = false;
    public static volatile boolean createSceneViewModelStoreBySceneSelf = false;
    public static volatile boolean useActivityCompatibleLifecycleStrategy = false;
    public static volatile boolean sceneLifecycleCallbackObjectCreationOpt = false;
}