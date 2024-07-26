package com.bytedance.scene.logger;

/**
 * Created by jiangqi on 2023/11/5
 *
 * @author jiangqi@bytedance.com
 */
public interface Logger {
    void v(String tag, String msg);

    void d(String tag, String msg);

    void i(String tag, String msg);

    void e(String tag, String msg);
}
