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

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.os.CancellationSignal;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneParent;

import java.util.List;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 7/30/18.
 */

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class Utility {
    public static Drawable getWindowBackground(Context context) {
        if (context instanceof Activity) {
            throw new IllegalArgumentException("Use Scene Context instead");
        }
        TypedValue a = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            int color = a.data;
            return new ColorDrawable(color);
        } else {
            // windowBackground is not a color, probably a drawable
            Drawable d = context.getResources().getDrawable(a.resourceId);
            return d;
        }
    }

    public static void executeImmediatelyOrOnPreDraw(@NonNull final View view, boolean force, @Nullable final Runnable action) {
        if (view == null) {
            return;
        }
        if (force || view.getWidth() > 0 && view.getHeight() > 0) {
            if (action != null) {
                action.run();
            }
        } else {
            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (action != null) {
                        action.run();
                    }
                    return true;
                }
            });
            view.invalidate();
        }
    }

    /**
     * This is especially error-prone, so the ViewTreeObserver parameter is mandatory.
     * <p>
     * If a View does not have attachedWindow(), the getViewTreeObserver is created internally by itself,
     * and ready to be merged into the system when attachedToWindow.
     * But if you need to remove this View in somewhere (such as Scene's own lifecycle)
     * Then there is no way to getViewTreeObserver.removeOnPreDrawListener(this)
     */
    public static void executeImmediatelyOrOnPreDraw(@NonNull final View view, @NonNull final ViewTreeObserver viewTreeObserver, boolean force, @NonNull final CancellationSignal cancellationSignal, @NonNull final Runnable action) {
        if (view == null) {
            throw new NullPointerException("view can't be null");
        }
        if (viewTreeObserver == null) {
            throw new NullPointerException("viewTreeObserver can't be null");
        }
        if (action == null) {
            throw new NullPointerException("action can't be null");
        }
        if (force || view.getWidth() > 0 && view.getHeight() > 0) {
            action.run();
        } else {
            viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    viewTreeObserver.removeOnPreDrawListener(this);
                    if (!cancellationSignal.isCanceled()) {
                        action.run();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            view.invalidate();
        }
    }

    public static void executeOnPreDraw(@NonNull final View view, @Nullable final Runnable action) {
        if (view == null) {
            return;
        }
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                if (action != null) {
                    action.run();
                }
                return true;
            }
        });
        view.invalidate();
    }

    public static void removeFromParentView(View view) {
        ViewParent viewParent = view.getParent();
        if (viewParent instanceof ViewGroup) {
            ((ViewGroup) viewParent).removeView(view);
        }
    }

    public static boolean isActivityStatusValid(@Nullable Activity activity) {
        if (activity == null) {
            return false;
        }
        if (activity.isFinishing()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
            return false;
        }
        return true;
    }

    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }

    public static String requireNonEmpty(String obj, String message) {
        if (TextUtils.isEmpty(obj))
            throw new IllegalArgumentException(message);
        return obj;
    }

    public static void commitFragment(@NonNull FragmentManager fragmentManager, @NonNull FragmentTransaction transaction, boolean commitNow) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (commitNow) {
                transaction.commitNowAllowingStateLoss();
            } else {
                transaction.commitAllowingStateLoss();
            }
        } else {
            transaction.commitAllowingStateLoss();
            if (commitNow) {
                fragmentManager.executePendingTransactions();
            }
        }
    }

    public static void buildShortClassTag(Object cls, StringBuilder out) {
        if (cls == null) {
            out.append("null");
        } else {
            String simpleName = cls.getClass().getSimpleName();
            if (simpleName == null || simpleName.length() <= 0) {
                simpleName = cls.getClass().getName();
                int end = simpleName.lastIndexOf('.');
                if (end > 0) {
                    simpleName = simpleName.substring(end + 1);
                }
            }
            out.append(simpleName);
            out.append('{');
            out.append(Integer.toHexString(System.identityHashCode(cls)));
            out.append('}');
        }
    }

    @NonNull
    public static String getIdName(@NonNull Context context, @IdRes int viewId) {
        String resName;
        try {
            resName = context.getResources().getResourceName(viewId);
        } catch (Resources.NotFoundException e) {
            resName = String.valueOf(viewId);
        }
        return resName;
    }

    public static String getViewHierarchy(@NonNull Scene scene) {
        StringBuilder desc = new StringBuilder();
        getViewHierarchy(scene, desc, 0);
        return desc.toString();
    }

    private static void getViewHierarchy(Scene scene, StringBuilder desc, int margin) {
        desc.append(getViewMessage(scene, margin));
        if (scene instanceof SceneParent) {
            margin++;
            SceneParent sceneParent = (SceneParent) scene;
            List<Scene> list = sceneParent.getSceneList();
            for (int i = 0; i < list.size(); i++) {
                getViewHierarchy(list.get(i), desc, margin);
            }
        }
    }

    private static String getViewMessage(Scene scene, int marginOffset) {
        Scene parentScene = scene.getParentScene();
        String repeated = new String(new char[marginOffset]).replace("\0", "    ");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(repeated + "[" + scene.getClass().getSimpleName() + "] ");

        if (parentScene != null) {
            stringBuilder.append(((SceneParent) parentScene).getSceneDebugInfo(scene));
        }

        String resourceId = null;
        if (scene.getApplicationContext() != null && scene.getView() != null && scene.getView().getId() != View.NO_ID) {
            resourceId = getIdName(scene.requireApplicationContext(), scene.getView().getId());
        }
        if (resourceId != null) {
            stringBuilder.append("viewId: " + resourceId + " ");
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    public static int getColorWithAlpha(float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        return a + rgb;
    }
}
