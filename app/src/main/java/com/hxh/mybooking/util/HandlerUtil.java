package com.hxh.mybooking.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by HXH on 2018/6/29 0025.
 * Handler工具
 */
public class HandlerUtil {
    public static final Handler HANDLER = new Handler(Looper.getMainLooper());

    public static void runOnUiThread(Runnable runnable) {
        HANDLER.post(runnable);
    }

    public static void runOnUiThreadDelay(Runnable runnable, long delayMillis) {
        HANDLER.postDelayed(runnable, delayMillis);
    }

    public static void removeRunnable(Runnable runnable) {
        HANDLER.removeCallbacks(runnable);
    }
}
