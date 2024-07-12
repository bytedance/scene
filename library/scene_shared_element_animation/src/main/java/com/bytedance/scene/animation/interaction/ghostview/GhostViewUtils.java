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
package com.bytedance.scene.animation.interaction.ghostview;

import android.graphics.Matrix;
import android.os.Build;

import androidx.annotation.RestrictTo;

import android.view.View;
import android.view.ViewGroup;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import com.bytedance.scene.animation.interaction.transition.GhostViewUtilsCompatibleInvokeLayer;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class GhostViewUtils {
    private static final GhostViewImpl.Creator CREATOR;

    static {
        if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT <= 28) {
            CREATOR = new GhostViewApi21.Creator();
        } else {
            CREATOR = null;
        }
    }

    public static void addGhost(View view, ViewGroup viewGroup, Matrix matrix) {
        if (CREATOR != null) {
            CREATOR.addGhost(view, viewGroup, matrix);
        } else {
            GhostViewUtilsCompatibleInvokeLayer.addGhost(view, viewGroup, matrix);
        }
    }

    public static void removeGhost(View view) {
        if (CREATOR != null) {
            CREATOR.removeGhost(view);
        } else {
            GhostViewUtilsCompatibleInvokeLayer.removeGhost(view);
        }
    }
}
