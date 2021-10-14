package com.bytedance.scene.navigation.utility;

public class TestUtility {
    public static void forceGc() {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
    }

    private TestUtility() {
    }
}