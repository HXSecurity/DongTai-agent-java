package io.dongtai.iast.agent.util;

import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.common.entity.performance.metrics.ThreadInfoMetrics;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

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
    public static List<ThreadInfoMetrics.ThreadInfo> getDongTaiCoreThreads() {
        List<ThreadInfoMetrics.ThreadInfo> dongTaiThreadInfoList = new ArrayList<ThreadInfoMetrics.ThreadInfo>();
        try {
            Thread[] threads = getCurrentActiveThreads();
            for (Thread thread : threads) {
                // 匹配DongTai线程
                if (thread.getName() == null || !thread.getName().startsWith(AgentConstant.THREAD_NAME_PREFIX_CORE)) {
                    continue;
                }
                ThreadInfoMetrics.ThreadInfo dongTaiThread = new ThreadInfoMetrics.ThreadInfo();
                dongTaiThread.setId(thread.getId());
                dongTaiThread.setName(thread.getName());
                dongTaiThread.setCpuTime(getThreadCpuTime(thread.getId()));
                dongTaiThreadInfoList.add(dongTaiThread);
            }
            // 停顿时间间隔,用于收集cpu使用率变化
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignore) {
            }
            // 收集洞态线程最新的cpu时间
            for (ThreadInfoMetrics.ThreadInfo dongTaiThread : dongTaiThreadInfoList) {
                dongTaiThread.setCpuTime(getThreadCpuTime(dongTaiThread.getId()));
            }
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.AGENT_GET_DONGTAI_THREAD_FAILED, e);
        }
        return dongTaiThreadInfoList;
    }

    /**
     * 杀死指定的洞态线程
     *
     * @param threadId 线程id
     * @return boolean
     */
    public static boolean killDongTaiCoreThread(Long threadId) {
        try {
            if (threadId == null || threadId <= 0) {
                return false;
            }
            final Thread[] threads = getCurrentActiveThreads();
            for (Thread thread : threads) {
                if (thread.getId() == threadId && thread.getName() != null
                        && thread.getName().startsWith(AgentConstant.THREAD_NAME_PREFIX_CORE)) {
                    thread.interrupt();
                    return true;
                }
            }
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.AGENT_KILL_DONGTAI_CORE_THREAD_FAILED, e);
        }
        return false;
    }

    /**
     * 杀死所有的洞态线程
     */
    public static void killAllDongTaiCoreThreads() {
        List<ThreadInfoMetrics.ThreadInfo> dongTaiThreads = ThreadUtils.getDongTaiCoreThreads();
        for (ThreadInfoMetrics.ThreadInfo each : dongTaiThreads) {
            ThreadUtils.killDongTaiCoreThread(each.getId());
        }
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

    /**
     * 获取当前活动线程
     *
     * @return {@link Thread[]}
     */
    private static Thread[] getCurrentActiveThreads() {
        final ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int threadNum = currentGroup.activeCount();
        Thread[] threads = new Thread[threadNum];
        currentGroup.enumerate(threads);
        return threads;
    }

    private static Long getThreadCpuTime(long threadId) {
        try {
            return ManagementFactory.getThreadMXBean().getThreadCpuTime(threadId);
        } catch (Throwable ignored) {
            return null;
        }
    }


}
