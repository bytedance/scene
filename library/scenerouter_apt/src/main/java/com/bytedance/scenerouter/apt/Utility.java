package com.bytedance.scenerouter.apt;

public class Utility {
    private Utility() {

    }

    public static boolean isEmpty(String text) {
        if (text == null || text.trim().length() == 0) {
            return true;
        } else {
            return false;
        }
    }
}
