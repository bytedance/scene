package com.bytedance.scene;

import android.os.Build;
import com.google.auto.service.AutoService;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.pluginapi.config.GlobalConfigProvider;

@AutoService(GlobalConfigProvider.class)
public class SceneConfigProvider implements GlobalConfigProvider {
    @Override
    public Config get() {
        RobolectricTestRunner.DeprecatedTestRunnerDefaultConfigProvider provider = new RobolectricTestRunner.DeprecatedTestRunnerDefaultConfigProvider();
//        return new Config.Builder(provider.get()).setSdk(Build.VERSION_CODES.KITKAT).build();
//        return new Config.Builder(provider.get()).setSdk(Build.VERSION_CODES.P).build();
//        return new Config.Builder(provider.get()).setMinSdk(Build.VERSION_CODES.JELLY_BEAN).setMaxSdk(Build.VERSION_CODES.P).build();
        return new Config.Builder(provider.get()).setSdk(
                Build.VERSION_CODES.JELLY_BEAN,
                Build.VERSION_CODES.JELLY_BEAN_MR1,
                Build.VERSION_CODES.JELLY_BEAN_MR2,
                Build.VERSION_CODES.KITKAT).build();

    }
}