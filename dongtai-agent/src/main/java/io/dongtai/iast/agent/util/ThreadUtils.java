package io.dongtai.iast.agent.util;

import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.common.entity.performance.metrics.ThreadInfoMetrics;
import io.dongtai.log.DongTaiLog;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * 线程工具类
 *
 * @author chenyi
 * @date 2022/03/03
 */
public class ThreadUtils {

    /**
     * 获取所有名称中包含DongTai字样的线程
     */
    public static List<ThreadInfoMetrics.ThreadInfo> getDongTaiThreads() {
        List<ThreadInfoMetrics.ThreadInfo> dongTaiThreadInfoList = new ArrayList<ThreadInfoMetrics.ThreadInfo>();
        try {
            final ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
            int threadNum = currentGroup.activeCount();
            Thread[] threads = new Thread[threadNum];
            currentGroup.enumerate(threads);
            for (int i = 0; i < threadNum; i++) {
                // 匹配DongTai线程
                if (threads[i].getName() != null && threads[i].getName().startsWith(Constant.THREAD_PREFIX)) {
                    ThreadInfoMetrics.ThreadInfo dongTaiThread = new ThreadInfoMetrics.ThreadInfo();
                    dongTaiThread.setId(threads[i].getId());
                    dongTaiThread.setName(threads[i].getName());
                    dongTaiThread.setCpuTime(getThreadCpuTime(threads[i].getId()));
                    dongTaiThreadInfoList.add(dongTaiThread);
                }
            }
            // 停顿时间间隔,用于收集cpu使用率变化
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
            // 收集洞态线程最新的cpu时间
            for (ThreadInfoMetrics.ThreadInfo dongTaiThread : dongTaiThreadInfoList) {
                dongTaiThread.setCpuTime(getThreadCpuTime(dongTaiThread.getId()));
            }
        } catch (Throwable t) {
            DongTaiLog.warn("Get DongTai thread failed, msg: {} , error: {}", t.getMessage(), t.getCause());
        }
        return dongTaiThreadInfoList;
    }

    public static void threadSleep(int seconds) {
        try {
            long milliseconds = seconds * 1000L;
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static Long getThreadCpuTime(long threadId) {
        try {
            return ManagementFactory.getThreadMXBean().getThreadCpuTime(threadId);
        } catch (Throwable ignored) {
            return null;
        }
    }


}
