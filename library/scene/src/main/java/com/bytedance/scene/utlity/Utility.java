package com.bytedance.scene.utlity;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.os.CancellationSignal;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 7/30/18.
 */
/** @hide */
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
     *
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
}
