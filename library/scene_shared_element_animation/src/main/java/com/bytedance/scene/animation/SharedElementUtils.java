package com.bytedance.scene.animation;

import android.annotation.TargetApi;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.bytedance.scene.animation.interaction.ghostview.GhostViewUtils;
import com.bytedance.scene.utlity.NonNullPair;

import java.util.*;

@TargetApi(21)
public class SharedElementUtils {
    /**
     * TODO: What if it is the parent of the shared element?
     * RootView must be excluded, The rootView does an alpha animation equal to the system's captureTransitioningViews.
     */
    public static List<View> captureTransitioningViews(View view, View rootView) {
        List<View> list = new ArrayList<>();
        captureTransitioningViews(view, rootView, list);
        return list;
    }

    private static void captureTransitioningViews(View view, View rootView, List<View> list) {
        if (view.getVisibility() != View.VISIBLE) {
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            if (viewGroup.isTransitionGroup() && view != rootView) {
                list.add(viewGroup);
            } else {
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    captureTransitioningViews(viewGroup.getChildAt(i), rootView, list);
                }
            }
        } else {
            if (view.getVisibility() == View.VISIBLE) {
                list.add(view);
            }
        }
    }

    public static void moveViewToOverlay(List<View> viewList, ViewGroup dstViewGroup) {
        for (View view : viewList) {
            GhostViewUtils.addGhost(view, dstViewGroup, null);
        }
    }

    public static void moveViewFromOverlay(List<View> viewList) {
        for (View view : viewList) {
            GhostViewUtils.removeGhost(view);
        }
    }

    public static void moveViewToOverlay(View view, ViewGroup dstViewGroup, Matrix matrix) {
        GhostViewUtils.addGhost(view, dstViewGroup, matrix);
    }

    public static void moveViewFromOverlay(View view) {
        GhostViewUtils.removeGhost(view);
    }

    public static View getViewByTransitionName(View view, String transitionName, boolean visible) {
        if (transitionName.equals(ViewCompat.getTransitionName(view))) {
            return view;
        } else if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View childView = ((ViewGroup) view).getChildAt(i);
                if (childView.getVisibility() == View.VISIBLE || !visible) {
                    View targetView = getViewByTransitionName(childView, transitionName, visible);
                    if (targetView != null) {
                        return targetView;
                    }
                }
            }
        }
        return null;
    }

    private static HashMap<String, View> getTransitionViewList(View view, List<String> transitionNameList, boolean visible) {
        transitionNameList = new ArrayList<>(new HashSet<>(transitionNameList));
        HashMap<String, View> hashMap = new HashMap<>();
        for (String transitionName : transitionNameList) {
            View targetView = SharedElementUtils.getViewByTransitionName(view, transitionName, visible);
            if (targetView != null) {
                hashMap.put(transitionName, targetView);
            }
        }
        return hashMap;
    }

    private static HashMap<String, Pair<View, View>> getShareView(View fromView, View toView, List<String> share) {
        HashMap<String, View> fromList = getTransitionViewList(fromView, share, true);
        HashMap<String, View> toList = getTransitionViewList(toView, share, true);

        HashMap<String, Pair<View, View>> hashMap = new HashMap<>();
        for (Map.Entry<String, View> entry : fromList.entrySet()) {
            String key = entry.getKey();
            View toTargetView = toList.get(key);
            if (toTargetView != null) {
                hashMap.put(key, Pair.create(entry.getValue(), toTargetView));
            }
        }
        return hashMap;
    }

    private static NonNullPair<View, View> getShareView(View fromView, View toView, String share) {
        return NonNullPair.create(SharedElementUtils.getViewByTransitionName(fromView, share, true), SharedElementUtils.getViewByTransitionName(toView, share, true));
    }

    public static NonNullPair<List<View>, List<View>> stripOffscreenViews(List<View> viewList) {
        List<View> transitioningViews = new ArrayList<>(viewList);
        List<View> strippedTransitioningViews = new ArrayList<>();
        Rect r = new Rect();
        for (int i = transitioningViews.size() - 1; i >= 0; i--) {
            View view = transitioningViews.get(i);
            if (!view.getGlobalVisibleRect(r)) {
                transitioningViews.remove(i);
                strippedTransitioningViews.add(view);
            }
        }
        return NonNullPair.create(transitioningViews, strippedTransitioningViews);
    }

    /**
     * Guarantee order: Parent -> Child
     * Make sure that Parent will not overwrite Child when adding Overlay
     */
    public static List<NonNullPair<String, View>> sortSharedElementList(ArrayMap<String, View> sharedElements) {
        List<NonNullPair<String, View>> list = new ArrayList<>();
        boolean isFirstRun = true;
        while (!sharedElements.isEmpty()) {
            final int numSharedElements = sharedElements.size();
            for (int i = numSharedElements - 1; i >= 0; i--) {
                final View view = sharedElements.valueAt(i);
                final String name = sharedElements.keyAt(i);
                if (isFirstRun && (view == null || !view.isAttachedToWindow() || name == null)) {
                    sharedElements.removeAt(i);
                } else if (!isNested(view, sharedElements)) {
                    list.add(NonNullPair.create(name, view));
                    sharedElements.removeAt(i);
                }
            }
            isFirstRun = false;
        }
        return list;
    }

    private static boolean isNested(View view, ArrayMap<String, View> sharedElements) {
        ViewParent parent = view.getParent();
        boolean isNested = false;
        while (parent instanceof View) {
            View parentView = (View) parent;
            if (sharedElements.containsValue(parentView)) {
                isNested = true;
                break;
            }
            parent = parentView.getParent();
        }
        return isNested;
    }
}
