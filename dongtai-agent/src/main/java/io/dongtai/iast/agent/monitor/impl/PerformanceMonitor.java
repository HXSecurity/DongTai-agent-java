package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.fallback.FallbackManager;
import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.monitor.collector.IPerformanceCollector;
import io.dongtai.iast.agent.monitor.collector.MetricsBindCollectorEnum;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.CpuInfoMetrics;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;
import io.dongtai.iast.common.enums.MetricsKey;
import io.dongtai.iast.common.utils.serialize.SerializeUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 负责监控jvm性能状态，如果达到停止阈值，则停止检测引擎；如果达到卸载阈值，则卸载引擎；
 *
 * @author dongzhiyong@huoxian.cn
 */
public class PerformanceMonitor implements IMonitor {
    private static Integer CPU_USAGE = 0;
    private static MemoryUsageMetrics MEMORY_USAGE = null;
    private static List<PerformanceMetrics> PERFORMANCE_METRICS = new ArrayList<PerformanceMetrics>();

    private static final String NAME = "PerformanceMonitor";
    private final EngineManager engineManager;
    private final List<MetricsKey> needCollectMetrics = new ArrayList<MetricsKey>();

    @Override
    public String getName() {
        return AgentConstant.THREAD_NAME_PREFIX + NAME;
    }

    public static void setPerformanceMetrics(List<PerformanceMetrics> performanceMetrics) {
        PERFORMANCE_METRICS = performanceMetrics;
    }

    public PerformanceMonitor(EngineManager engineManager) {
        this.engineManager = engineManager;
        configCollectMetrics();
    }

    /**
     * 配置需要收集的指标(todo:通过配置文件初始化)
     */
    private void configCollectMetrics() {
        needCollectMetrics.add(MetricsKey.CPU_USAGE);
        needCollectMetrics.add(MetricsKey.MEM_USAGE);
    }

    public static Integer getCpuUsage() {
        return CPU_USAGE;
    }

    public static MemoryUsageMetrics getMemoryUsage() {
        return MEMORY_USAGE;
    }

    public static Integer getDiskUsage() {
        try {
            File[] files = File.listRoots();
            for (File file : files) {
                double rate = ((file.getTotalSpace() - file.getUsableSpace()) * 1.0 / file.getTotalSpace()) * 100;
                return (int) rate;
            }
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.AGENT_MONITOR_GET_DISK_USAGE_FAILED,
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
        return 0;
    }

    public static List<PerformanceMetrics> getPerformanceMetrics() {
        if (PERFORMANCE_METRICS == null) {
            PERFORMANCE_METRICS = new ArrayList<PerformanceMetrics>();
        }
        return PERFORMANCE_METRICS;
    }

    /**
     * 状态发生转换时，触发engineManager的操作
     * <p>
     * 状态维护：
     * 0 -> 1 -> 0
     */
    @Override
    public void check() {
        try {
            // collect performance metrics
            final List<PerformanceMetrics> performanceMetrics = collectPerformanceMetrics();
            // update local performance metrics for scheduled reporting
            updatePerformanceMetrics(performanceMetrics);
            // check performance metrics for fallback
            checkPerformanceMetrics(performanceMetrics);
        } catch (Throwable t) {
            DongTaiLog.warn(ErrorCode.AGENT_MONITOR_THREAD_CHECK_FAILED, getName(), t);
        }
    }

    private void updatePerformanceMetrics(List<PerformanceMetrics> performanceMetrics) {
        for (PerformanceMetrics metrics : performanceMetrics) {
            if (metrics.getMetricsKey() == MetricsKey.CPU_USAGE) {
                final CpuInfoMetrics cpuInfoMetrics = metrics.getMetricsValue(CpuInfoMetrics.class);
                CPU_USAGE = cpuInfoMetrics.getCpuUsagePercentage().intValue();
            } else if (metrics.getMetricsKey() == MetricsKey.MEM_USAGE) {
                MEMORY_USAGE = metrics.getMetricsValue(MemoryUsageMetrics.class);
            }
        }
        PERFORMANCE_METRICS = performanceMetrics;
    }


    /**
     * 收集性能指标
     *
     * @return {@link List}<{@link PerformanceMetrics}>
     */
    private List<PerformanceMetrics> collectPerformanceMetrics() {
        final List<PerformanceMetrics> metricsList = new ArrayList<PerformanceMetrics>();
        for (MetricsKey metricsKey : needCollectMetrics) {
            final MetricsBindCollectorEnum collectorEnum = MetricsBindCollectorEnum.getEnum(metricsKey);
            if (collectorEnum == null) {
                continue;
            }
            try {
                IPerformanceCollector collector = collectorEnum.getCollector().newInstance();
                metricsList.add(collector.getMetrics());
            } catch (Throwable t) {
                DongTaiLog.warn(ErrorCode.AGENT_MONITOR_COLLECT_PERFORMANCE_METRICS_FAILED, collectorEnum, t.getMessage());
            }
        }
        return metricsList;
    }

    /**
     * 寻找性能监控熔断器类,反射调用进行性能熔断检查
     */
    private void checkPerformanceMetrics(List<PerformanceMetrics> performanceMetrics) {
        try {
            FallbackManager.invokePerformanceBreakerCheck(SerializeUtils.serializeByList(performanceMetrics));
        } catch (Throwable t) {
            DongTaiLog.warn(ErrorCode.AGENT_MONITOR_CHECK_PERFORMANCE_METRICS_FAILED,
                    t.getMessage(), t.getCause().getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (!MonitorDaemonThread.isExit) {
                this.check();
                ThreadUtils.threadSleep(30);
            }
        } catch (Throwable t) {
            DongTaiLog.debug("{} interrupted: {}", getName(), t.getMessage());
        }
    }
}
