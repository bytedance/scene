package com.bytedance.scene;

/**
 * Created by fengminchao on 2023/10/18
 *
 * @author fengminchao@bytedance.com
 */
public class SceneGlobalConfig {
    public static volatile int genScopeStrategy = 0;

    public static volatile boolean validateSceneViewModelProvidersMainThreadStrategy = false;

    public static volatile boolean createSceneViewModelStoreBySceneSelf = false;
}