package com.hxh.mybooking.thread;

import com.hxh.mybooking.util.HandlerUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by HXH on 2018/8/25
 * 多线程帮助类
 */
public class MultiThreadHelper {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 开启新线程，处理耗时操作
     *
     * @param listener
     * @param <T>
     */
    public static synchronized <T> void execute(RunOrUpdateListener<T> listener) {
        executorService.execute(new ChildThread<T>(listener));
        //new DBThread(listener).start();
    }

    /**
     * @param <T>
     * @see@link #execute(RunOrUpdateListener) 处理耗时操作回调接口
     */
    public interface RunOrUpdateListener<T> {
        /**
         * 线程内执行
         *
         * @return
         */
        T onRunInThread();

        /**
         * 完成线程内执行后，返回T类型obj，主线程回调
         *
         * @param obj
         */
        void onRunUiThread(T obj);
    }

    /**
     * 不关心主线程回调的处理耗时操作接口
     *
     * @param <T>
     */
    public static abstract class SimpleRunOrUpdate<T> implements RunOrUpdateListener<T> {
        @Override
        public void onRunUiThread(T obj) {

        }
    }

    /**
     * @author HXH
     * @类说明 子线程类
     */
    private static class ChildThread<T> extends Thread {
        private RunOrUpdateListener<T> listener;

        public ChildThread(RunOrUpdateListener<T> listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            final T obj = listener.onRunInThread();
            HandlerUtil.runOnUiThread(() -> listener.onRunUiThread(obj));
        }
    }

}
