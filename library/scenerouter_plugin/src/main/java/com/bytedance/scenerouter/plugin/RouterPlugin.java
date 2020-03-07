package com.bytedance.scenerouter.plugin;

import com.android.build.gradle.BaseExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class RouterPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        BaseExtension baseExtension = project.getExtensions().getByType(BaseExtension.class);
        if (baseExtension == null) {
            return;
        }
        RouterPluginExtension extension = project.getExtensions()
                .create("sceneRouter", RouterPluginExtension.class);
        baseExtension.registerTransform(new RouterTransform(extension.debug));
    }
}
