package io.dongtai.iast.agent.monitor.collector.impl;

import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.ThreadInfoMetrics;
import io.dongtai.iast.common.enums.MetricsKey;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * 线程信息收集器
 *
 * @author chenyi
 * @date 2022/3/1
 * @see java.lang.management.ThreadMXBean
 */
public class ThreadInfoCollector extends AbstractPerformanceCollector {

    @Override
    public PerformanceMetrics getMetrics() {
        final ThreadInfoMetrics metricsValue = new ThreadInfoMetrics();
        final ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        metricsValue.setThreadCount(threadMxBean.getThreadCount());
        metricsValue.setPeakThreadCount(threadMxBean.getPeakThreadCount());
        metricsValue.setDaemonThreadCount(threadMxBean.getDaemonThreadCount());
        metricsValue.setDongTaiThreadInfoList(ThreadUtils.getDongTaiThreads());
        return buildMetricsData(MetricsKey.THREAD_INFO, metricsValue);
    }
}
