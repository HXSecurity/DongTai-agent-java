package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.breaker;


import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.CpuInfoMetrics;
import io.dongtai.iast.common.entity.performance.metrics.GarbageInfoMetrics;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;
import io.dongtai.iast.common.entity.performance.metrics.ThreadInfoMetrics;
import io.dongtai.iast.common.enums.MetricsKey;
import io.dongtai.iast.common.utils.serialize.SerializeUtils;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.checker.IPerformanceChecker;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.checker.MetricsBindCheckerEnum;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.PerformanceLimitReport;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.body.PerformanceBreakReportBody;
import io.dongtai.iast.core.utils.json.GsonUtils;
import io.dongtai.log.DongTaiLog;

import java.io.Serializable;
import java.util.*;

/**
 * 熔断器空实现(该实现不会进行任何操作)
 *
 * @author chenyi
 * @date 2022/2/28
 */
public class NopBreaker extends AbstractBreaker {

    private static Properties cfg;

    private static boolean isFallback = false;

    protected NopBreaker(Properties cfg) {
        super(cfg);
    }

    @Override
    protected void initBreaker(Properties cfg) {
        NopBreaker.cfg = cfg;
    }

    @Override
    public void breakCheck(String contextString) {
        checkMetricsWithAutoFallback(contextString);
    }

    @Override
    public void switchBreaker(boolean turnOn) {

    }

    public static boolean checkMetricsWithAutoFallback(String contextString) {
        boolean lastFallback;
        if (isFallback){
            lastFallback = true;
        }else {
            lastFallback = false;
        }
        isFallback = false;
        List<PerformanceMetrics> performanceMetrics = convert2MetricsList(contextString);
        // 检查每个性能是否达到限制值
        for (PerformanceMetrics metrics : performanceMetrics) {
            final IPerformanceChecker performanceChecker = MetricsBindCheckerEnum.newCheckerInstance(metrics.getMetricsKey());
            if (performanceChecker != null && performanceChecker.isPerformanceOverLimit(metrics, cfg)) {
                final PerformanceMetrics threshold = performanceChecker.getMatchMaxThreshold(metrics.getMetricsKey(), cfg);
                appendToOverThresholdLog(false, metrics, threshold, 1);
                isFallback = true;
                EngineManager.turnOffEngine();
                DongTaiLog.info("performance is over max threshold! metrics:" + GsonUtils.toJson(metrics));
                DongTaiLog.info("Engine performance fallback is open, Fallback engine close successfully");
            }
        }
        if (!isFallback && lastFallback){
            DongTaiLog.info("Engine performance fallback is close, Fallback engine open successfully");
        }
        return true;
    }

    /**
     * 将上下文转换为指标列表
     *
     * @param contextString 上下文字符串
     * @return {@link List}<{@link PerformanceMetrics}> 指标列表
     */
    public static List<PerformanceMetrics> convert2MetricsList(String contextString) {
        try {
            final List<Class<? extends Serializable>> clazzWhiteList = Arrays.asList(PerformanceMetrics.class, MetricsKey.class,
                    CpuInfoMetrics.class, MemoryUsageMetrics.class, GarbageInfoMetrics.class, GarbageInfoMetrics.CollectionInfo.class,
                    ThreadInfoMetrics.class, ThreadInfoMetrics.ThreadInfo.class);
            return SerializeUtils.deserialize2ArrayList(contextString, PerformanceMetrics.class, clazzWhiteList);
        } catch (Exception e) {
            DongTaiLog.warn("convert2MetricsList failed, err:{}", e.getMessage());
            return new ArrayList<PerformanceMetrics>();
        }
    }

    /**
     * 追加性能超限日志记录
     *
     * @param isRisk       是否是风险阈值
     * @param nowMetrics   当前指标
     * @param threshold    阈值指标
     * @param metricsCount 超限的指标数
     */
    private static void appendToOverThresholdLog(boolean isRisk, PerformanceMetrics nowMetrics, PerformanceMetrics threshold, Integer metricsCount) {
        final PerformanceBreakReportBody.PerformanceOverThresholdLog breakLog = new PerformanceBreakReportBody.PerformanceOverThresholdLog();
        breakLog.setDate(new Date());
        breakLog.setOverThresholdType(isRisk ? 1 : 2);
        breakLog.setNowMetrics(nowMetrics);
        breakLog.setThreshold(threshold);
        breakLog.setOverThresholdCount(metricsCount);
        PerformanceLimitReport.appendPerformanceBreakLog(breakLog);
    }
}
