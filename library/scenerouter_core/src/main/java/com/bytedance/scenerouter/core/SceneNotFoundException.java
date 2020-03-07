package com.bytedance.scenerouter.core;

public final class SceneNotFoundException extends Exception {
    private SceneNotFoundException() {

    }

    public static final SceneNotFoundException INSTANCE = new SceneNotFoundException();
}
