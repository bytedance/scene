package com.bytedance.scene.navigation;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior;
import com.bytedance.scene.utlity.ThreadUtility;

import java.util.WeakHashMap;

/**
 * Created by jiangqi on 2024/7/4
 *
 * @author jiangqi@bytedance.com
 * @hide
 */
@RestrictTo(LIBRARY)
public class ActivityCompatibleInfoCollector {

    public static class Holder {
        public Integer configChanges;
    }

    private static final WeakHashMap<Scene, Holder> sSceneHolderWeakHashMap = new WeakHashMap<>();

    @Nullable
    public static Holder getHolder(@NonNull Scene scene) {
        if (scene == null) {
            throw new NullPointerException("Scene can't be null");
        }
        if (!isTargetSceneType(scene)) {
            return null;
        }
        if (sSceneHolderWeakHashMap.size() == 0) {
            return null;
        }
        return sSceneHolderWeakHashMap.get(scene);
    }

    @NonNull
    public static Holder getOrCreateHolder(@NonNull Scene scene) {
        if (scene == null) {
            throw new NullPointerException("Scene can't be null");
        }
        if (!isTargetSceneType(scene)) {
            throw new NullPointerException("Scene must implement ActivityCompatibleBehavior");
        }
        ThreadUtility.checkUIThread();
        Holder holder = sSceneHolderWeakHashMap.get(scene);
        if (holder == null) {
            holder = new Holder();
            sSceneHolderWeakHashMap.put(scene, holder);
        }
        return holder;
    }

    public static void clearHolder(@NonNull Scene scene) {
        if (!isTargetSceneType(scene)) {
            return;
        }
        ThreadUtility.checkUIThread();
        if (sSceneHolderWeakHashMap.size() > 0) {
            sSceneHolderWeakHashMap.remove(scene);
        }
    }

    public static boolean containsConfigChanges(@NonNull Scene scene) {
        if (scene == null) {
            throw new NullPointerException("Scene can't be null");
        }
        if (!isTargetSceneType(scene)) {
            return false;
        }
        if (sSceneHolderWeakHashMap.size() == 0) {
            return false;
        }
        Holder holder = sSceneHolderWeakHashMap.get(scene);
        return holder != null && holder.configChanges != null;
    }

    private static boolean isTargetSceneType(@NonNull Scene scene){
        return scene instanceof ActivityCompatibleBehavior;
    }
}
