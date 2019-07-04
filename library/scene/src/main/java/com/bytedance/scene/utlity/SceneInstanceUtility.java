package com.bytedance.scene.utlity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.util.SimpleArrayMap;

import com.bytedance.scene.Scene;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 9/10/18.
 */

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class SceneInstanceUtility {
    private static final SimpleArrayMap<String, Class<?>> sClassMap =
            new SimpleArrayMap<String, Class<?>>();

    public static Scene getInstanceFromClassName(Context context, String clazzName, Bundle arguments) {
        Class<?> clazz = sClassMap.get(clazzName);
        try {
            if (clazz == null) {
                clazz = context.getClassLoader().loadClass(clazzName);
                sClassMap.put(clazzName, clazz);
            }
            return getInstanceFromClass(clazz, arguments);
        } catch (ClassNotFoundException e) {
            throw new InstantiationException("Unable to instantiate scene " + clazzName
                    + ": make sure class name exists, is public, and has an"
                    + " empty constructor that is public", e);
        }
    }

    public static Scene getInstanceFromClass(@NonNull Class<?> clazz, @Nullable Bundle arguments) {
        try {
            Scene scene = (Scene) clazz.getConstructor().newInstance();
            if (arguments != null) {
                arguments.setClassLoader(scene.getClass().getClassLoader());
                scene.setArguments(arguments);
            }
            return scene;
        } catch (java.lang.InstantiationException e) {
            throw new InstantiationException("Unable to instantiate scene " + clazz
                    + ": make sure class name exists, is public, and has an"
                    + " empty constructor that is public", e);
        } catch (IllegalAccessException e) {
            throw new InstantiationException("Unable to instantiate scene " + clazz
                    + ": make sure class name exists, is public, and has an"
                    + " empty constructor that is public", e);
        } catch (NoSuchMethodException e) {
            throw new InstantiationException("Unable to instantiate scene " + clazz
                    + ": could not find Scene constructor", e);
        } catch (InvocationTargetException e) {
            throw new InstantiationException("Unable to instantiate scene " + clazz
                    + ": calling Scene constructor caused an exception", e);
        }
    }

    //todo 检查是不是public/public static类
    public static boolean isSupportRestore(Scene scene) {
        Class<? extends Scene> clazz = scene.getClass();
        for (Constructor<?> constructor : clazz.getConstructors()) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length > 0) {
                return false;
            }
        }
        return true;
    }

    static public class InstantiationException extends RuntimeException {
        InstantiationException(String msg, Exception cause) {
            super(msg, cause);
        }
    }
}
