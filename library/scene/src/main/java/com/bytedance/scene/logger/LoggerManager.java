package com.bytedance.scene.logger;

import androidx.annotation.NonNull;

/**
 * Created by jiangqi on 2023/11/5
 *
 * @author jiangqi@bytedance.com
 */
public class LoggerManager {
    private static final LoggerManager sInstance = new LoggerManager();
    private static final String TAG = "Scene Framework";

    private LoggerManager() {

    }

    public static LoggerManager getInstance() {
        return sInstance;
    }

    private Logger mLogger = new DefaultLogger();

    public void setLogger(@NonNull Logger logger) {
        this.mLogger = logger;
    }

    public void v(String tag, String msg) {
        this.mLogger.v(TAG, tag + " " + msg);
    }

    public void d(String tag, String msg) {
        this.mLogger.v(TAG, tag + " " + msg);
    }

    public void i(String tag, String msg) {
        this.mLogger.v(TAG, tag + " " + msg);
    }

    public void e(String tag, String msg) {
        this.mLogger.v(TAG, tag + " " + msg);
    }
}
