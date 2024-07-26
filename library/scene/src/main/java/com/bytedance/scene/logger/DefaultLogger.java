package com.bytedance.scene.logger;

import android.util.Log;

/**
 * Created by jiangqi on 2023/11/5
 *
 * @author jiangqi@bytedance.com
 */
public class DefaultLogger implements Logger {
    @Override
    public void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    @Override
    public void d(String tag, String msg) {
        Log.v(tag, msg);
    }

    @Override
    public void i(String tag, String msg) {
        Log.v(tag, msg);
    }

    @Override
    public void e(String tag, String msg) {
        Log.v(tag, msg);
    }
}
