package com.bytedance.scene.utility;

public class TestUtility {
    public static void forceGc() {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
    }

    private TestUtility() {
    }
}