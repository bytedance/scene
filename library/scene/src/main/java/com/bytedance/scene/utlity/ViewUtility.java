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
package com.bytedance.scene.utlity;

import android.view.View;
import android.view.ViewParent;
import androidx.annotation.Nullable;
import com.bytedance.scene.R;
import com.bytedance.scene.Scene;

public class ViewUtility {
    @Nullable
    public static Scene findSceneByView(@Nullable View view) {
        while (view != null) {
            Scene scene = (Scene) view.getTag(R.id.bytedance_scene_view_scene_tag);
            if (scene != null) {
                return scene;
            } else {
                ViewParent viewParent = view.getParent();
                if (viewParent instanceof View) {
                    view = (View) viewParent;
                } else {
                    view = null;
                }
            }
        }
        return null;
    }
}
