package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker;

import io.dongtai.iast.common.entity.performance.metrics.GarbageInfoMetrics;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.enums.MetricsKey;
import io.dongtai.iast.common.utils.serialize.SerializeUtils;
import io.dongtai.log.DongTaiLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 默认的性能熔断器实现(仅支持JDK8+)
 *
 * @author chenyi
 * @date 2022/2/28
 */
public class DefaultPerformanceBreaker {

    public static void checkPerformance(String contextString) {
        List<PerformanceMetrics> performanceMetrics = convert2MetricsList(contextString);
        performanceMetrics.forEach((metrics) -> {
            //todo
            System.out.println(metrics.getMetricsKey().toString());
            System.out.println(metrics.getMetricsValue(metrics.getMetricsKey().getValueType()));
        });

    }

    /**
     * 将上下文转换为指标列表
     *
     * @param contextString 上下文字符串
     * @return {@link List}<{@link PerformanceMetrics}> 指标列表
     */
    private static List<PerformanceMetrics> convert2MetricsList(String contextString) {
        try {
            final List<Class<?>> clazzWhiteList = Arrays.asList(PerformanceMetrics.class, MetricsKey.class,
                    MemoryUsageMetrics.class, GarbageInfoMetrics.class, GarbageInfoMetrics.CollectionInfo.class);
            return SerializeUtils.deserialize2ArrayList(
                    contextString, PerformanceMetrics.class, clazzWhiteList);
        } catch (Exception e) {
            DongTaiLog.warn("convert2MetricsList failed, err:{}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
