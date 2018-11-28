package com.bytedance.scene;

import android.os.Bundle;

/**
 * Created by JiangQi on 10/25/18.
 */
public interface SceneComponentFactory {
    Scene instantiateScene(ClassLoader cl, String className, Bundle bundle);
}
