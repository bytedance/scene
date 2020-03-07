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
