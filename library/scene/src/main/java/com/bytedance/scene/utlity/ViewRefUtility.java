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

import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ViewRefUtility {
    private static final int NOT_INITIALIZED = 0;
    private static final int INIT_SUCCESS = 1;
    private static final int INIT_FAILED = 2;
    private static int sReflectedMethodInitialized = NOT_INITIALIZED;

    private static Method sCancelTouchTargetMethod = null;

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void cancelViewTouchTargetFromParent(@NonNull View view) {
        if (sReflectedMethodInitialized == NOT_INITIALIZED) {
            initializeReflectiveMethod();
        }

        if (sReflectedMethodInitialized == INIT_SUCCESS) {
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            try {
                sCancelTouchTargetMethod.invoke(viewGroup, view);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    //Android 4.1 - Android 10
    @MainThread
    private static void initializeReflectiveMethod() {
        try {
            sReflectedMethodInitialized = INIT_FAILED;
            sCancelTouchTargetMethod = ViewGroup.class.getDeclaredMethod("cancelTouchTarget", View.class);
            sCancelTouchTargetMethod.setAccessible(true);
            sReflectedMethodInitialized = INIT_SUCCESS;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
