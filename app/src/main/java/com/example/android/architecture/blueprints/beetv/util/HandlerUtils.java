package com.example.android.architecture.blueprints.beetv.util;

import com.example.android.architecture.blueprints.beetv.BeeTVApplication;

public class HandlerUtils {
    /**
     * 在主线程执行一段任务
     * @param r
     */
    public static void runOnMainThread(Runnable r) {
        runOnMainThread(r, 0);
    }

    public static void runOnMainThread(Runnable r, int time) {
        BeeTVApplication.mainHandler.postDelayed(r, time);
    }

    public static void removeOnMainCallback(Runnable r) {
        BeeTVApplication.mainHandler.removeCallbacks(r);
    }

}
