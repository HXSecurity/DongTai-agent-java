package io.dongtai.iast.core.utils;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.enums.MetricsKey;
import io.dongtai.iast.core.utils.json.GsonUtils;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

import java.util.*;

/**
 * 远端配置工具类
 *
 * @author chenyi
 * @date 2022/3/3
 */
public class RemoteConfigUtils {

    private RemoteConfigUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 全局配置
     */
    private static String existsRemoteConfigMeta = "{}";
    private static Boolean enableAutoFallback;
    /**
     * 高频hook限流相关配置
     */
    private static Double hookLimitTokenPerSecond;
    private static Double hookLimitInitBurstSeconds;
    /**
     * 高频流量限流相关配置
     */
    private static Double heavyTrafficLimitTokenPerSecond;
    private static Double heavyTrafficLimitInitBurstSeconds;
    private static Integer heavyTrafficBreakerWaitDuration;
    /**
     * 性能熔断阈值相关配置
     */
    private static Integer performanceBreakerWindowSize;
    private static Double performanceBreakerFailureRate;
    private static Integer performanceBreakerWaitDuration;
    private static Integer maxRiskMetricsCount;
    private static List<PerformanceMetrics> performanceLimitRiskThreshold;
    private static List<PerformanceMetrics> performanceLimitMaxThreshold;

    /**
     * 二次降级阈值相关配置
     */
    private static Double secondFallbackFrequencyTokenPerSecond;
    private static Double secondFallbackFrequencyInitBurstSeconds;
    private static Long secondFallbackDuration;


    /**
     * 同步远程配置
     *
     * @param remoteConfig 远程配置内容字符串
     */
    public static void syncRemoteConfig(String remoteConfig) {
        // 和上次配置内容不一致时才重新更新配置文件
        try {
            if (!existsRemoteConfigMeta.equals(remoteConfig)) {
                JSONObject configJson = new JSONObject(remoteConfig);
                enableAutoFallback = configJson.getBoolean("enableAutoFallback");
                hookLimitTokenPerSecond = configJson.getDouble("hookLimitTokenPerSecond");
                hookLimitInitBurstSeconds = configJson.getDouble("hookLimitInitBurstSeconds");
                performanceBreakerWindowSize = configJson.getInt("performanceBreakerWindowSize");
                performanceBreakerFailureRate = configJson.getDouble("performanceBreakerFailureRate");
                performanceBreakerWaitDuration = configJson.getInt("performanceBreakerWaitDuration");
                maxRiskMetricsCount = configJson.getInt("maxRiskMetricsCount");
                JSONObject perfLimMaxThresholdJson = configJson.getJSONObject("performanceLimitMaxThreshold");
                performanceLimitMaxThreshold = buildPerformanceMetricsFromJson(perfLimMaxThresholdJson.toString());
                JSONObject perfLimRiskThresholdJson = configJson.getJSONObject("performanceLimitRiskThreshold");
                performanceLimitRiskThreshold = buildPerformanceMetricsFromJson(perfLimRiskThresholdJson.toString());
                existsRemoteConfigMeta = remoteConfig;
                DongTaiLog.info("Sync remote config successful.");
            }
        } catch (Throwable t) {
            DongTaiLog.warn("Sync remote config failed, msg: {}, error: {}", t.getMessage(), t.getCause());
        }
    }

    /**
     * 将json转化为List<PerformanceMetrics>类型
     */
    private static List<PerformanceMetrics> buildPerformanceMetricsFromJson(String json){
        List<PerformanceMetrics> performanceMetricsList  = new ArrayList<>();
        try {
        JSONObject jsonObject = new JSONObject(json);
        Set<String> keySet = jsonObject.keySet();
            for (MetricsKey each : MetricsKey.values()) {
                PerformanceMetrics metrics = new PerformanceMetrics();
                if (keySet.contains(each.getKey())) {
                    String metricsValueJson = jsonObject.get(each.getKey()).toString();
                    metrics.setMetricsKey(each);
                    metrics.setMetricsValue(GsonUtils.toObject(metricsValueJson, each.getValueType()));
                    performanceMetricsList.add(metrics);
                }
            }
        }catch (Throwable t){
            DongTaiLog.warn("Build performance metrics from json failed, msg: {}, error: {}",t.getCause(),t.getMessage());
        }
        return performanceMetricsList;
    }

    // *************************************************************
    // 全局配置
    // *************************************************************

    /**
     * 是否允许自动降级
     */
    public static Boolean enableAutoFallback() {
        if (enableAutoFallback == null) {
            enableAutoFallback = PropertyUtils.getRemoteSyncLocalConfig("global.autoFallback", Boolean.class, true);
        }
        return enableAutoFallback;
    }

    // *************************************************************
    // 高频hook限流相关配置
    // *************************************************************

    /**
     * 高频hook限流-每秒获得令牌数
     */
    public static Double getHookLimitTokenPerSecond(Properties cfg) {
        if (hookLimitTokenPerSecond == null) {
            hookLimitTokenPerSecond = PropertyUtils.getRemoteSyncLocalConfig("hookLimit.tokenPerSecond", Double.class, 5000.0, cfg);
        }
        return hookLimitTokenPerSecond;
    }

    /**
     * 高频hook限流-初始预放置令牌时间
     */
    public static double getHookLimitInitBurstSeconds(Properties cfg) {
        if (hookLimitInitBurstSeconds == null) {
            hookLimitInitBurstSeconds = PropertyUtils.getRemoteSyncLocalConfig("hookLimit.initBurstSeconds", Double.class, 10.0, cfg);
        }
        return hookLimitInitBurstSeconds;
    }

    // *************************************************************
    // 高频流量限流相关配置
    // *************************************************************
    /**
     * 高频流量限流-每秒获得令牌数
     */
    public static Double getHeavyTrafficLimitTokenPerSecond(Properties cfg) {
        if (heavyTrafficLimitTokenPerSecond == null) {
            heavyTrafficLimitTokenPerSecond = PropertyUtils.getRemoteSyncLocalConfig("heavyTrafficLimit.tokenPerSecond", Double.class, 40.0, cfg);
        }
        return heavyTrafficLimitTokenPerSecond;
    }

    /**
     * 高频流量限流-初始预放置令牌时间
     */
    public static double getHeavyTrafficLimitInitBurstSeconds(Properties cfg) {
        if (heavyTrafficLimitInitBurstSeconds == null) {
            heavyTrafficLimitInitBurstSeconds = PropertyUtils.getRemoteSyncLocalConfig("heavyTrafficLimit.initBurstSeconds", Double.class, 2.0, cfg);
        }
        return heavyTrafficLimitInitBurstSeconds;
    }

    /**
     * 高频流量熔断器在 open 状态等待的时间，不能大于等于 secondFallbackDuration
     */
    public static int getHeavyTrafficBreakerWaitDuration(Properties cfg) {
        if (heavyTrafficBreakerWaitDuration == null) {
            heavyTrafficBreakerWaitDuration = PropertyUtils.getRemoteSyncLocalConfig("heavyTrafficLimit.heavyTrafficBreakerWaitDuration", Integer.class, 30, cfg);
        }
        return heavyTrafficBreakerWaitDuration;
    }


    // *************************************************************
    // 性能熔断阈值相关配置
    // *************************************************************

    /**
     * 获取性能断路器统计窗口大小
     */
    public static Integer getPerformanceBreakerWindowSize(Properties cfg) {
        if (performanceBreakerWindowSize == null) {
            performanceBreakerWindowSize = PropertyUtils.getRemoteSyncLocalConfig("performanceLimit.performanceBreakerWindowSize",
                    Integer.class, 2, cfg);
        }
        return performanceBreakerWindowSize;
    }

    public static void setPerformanceBreakerWindowSize(Integer performanceBreakerWindowSize) {
        RemoteConfigUtils.performanceBreakerWindowSize = performanceBreakerWindowSize;
    }

    /**
     * 获取性能断路器失败率阈值
     */
    public static Double getPerformanceBreakerFailureRate(Properties cfg) {
        if (performanceBreakerFailureRate == null) {
            performanceBreakerFailureRate = PropertyUtils.getRemoteSyncLocalConfig("performanceLimit.performanceBreakerFailureRate",
                    Double.class, 51.0, cfg);
        }
        return performanceBreakerFailureRate;
    }

    public static void setPerformanceBreakerFailureRate(Double performanceBreakerFailureRate) {
        RemoteConfigUtils.performanceBreakerFailureRate = performanceBreakerFailureRate;
    }

    /**
     * 获取性能断路器自动转半开的等待时间(单位:秒)
     */
    public static Integer getPerformanceBreakerWaitDuration(Properties cfg) {
        if (performanceBreakerWaitDuration == null) {
            performanceBreakerWaitDuration = PropertyUtils.getRemoteSyncLocalConfig("performanceLimit.performanceBreakerWaitDuration",
                    Integer.class, 40, cfg);
        }
        return performanceBreakerWaitDuration;
    }

    public static void setPerformanceBreakerWaitDuration(Integer performanceBreakerWaitDuration) {
        RemoteConfigUtils.performanceBreakerWaitDuration = performanceBreakerWaitDuration;
    }

    /**
     * 获取不允许超过风险阈值的指标数量(0为不限制，达到阈值数时熔断)
     */
    public static Integer getMaxRiskMetricsCount(Properties cfg) {
        if (maxRiskMetricsCount == null) {
            maxRiskMetricsCount = PropertyUtils.getRemoteSyncLocalConfig("performanceLimit.maxRiskMetricsCount", Integer.class, 3, cfg);
        }
        return maxRiskMetricsCount;
    }

    public static void setMaxRiskMetricsCount(Integer maxRiskMetricsCount) {
        RemoteConfigUtils.maxRiskMetricsCount = maxRiskMetricsCount;
    }

    /**
     * 获取性能限制风险阈值
     */
    public static List<PerformanceMetrics> getPerformanceLimitRiskThreshold(Properties cfg) {
        if (performanceLimitRiskThreshold == null) {
            performanceLimitRiskThreshold = buildPerformanceMetrics("performanceLimit.riskThreshold", cfg);
        }
        return performanceLimitRiskThreshold;
    }

    /**
     * 获取性能限制最大阈值
     */
    public static List<PerformanceMetrics> getPerformanceLimitMaxThreshold(Properties cfg) {
        if (performanceLimitMaxThreshold == null) {
            performanceLimitMaxThreshold = buildPerformanceMetrics("performanceLimit.maxThreshold", cfg);
        }
        return performanceLimitMaxThreshold;
    }

    // *************************************************************
    // 二次降级操作限流相关配置
    // *************************************************************
    /**
     * 二次降级限流令牌桶-每秒获得令牌数
     */
    public static Double getSwitchLimitTokenPerSecond(Properties cfg) {
        if (secondFallbackFrequencyTokenPerSecond == null) {
            secondFallbackFrequencyTokenPerSecond = PropertyUtils.getRemoteSyncLocalConfig("secondFallback.frequency.tokenPerSecond", Double.class, 0.01, cfg);
        }
        return secondFallbackFrequencyTokenPerSecond;
    }

    /**
     * 二次降级限流令牌桶-初始预放置令牌时间
     */
    public static double getSwitchLimitInitBurstSeconds(Properties cfg) {
        if (secondFallbackFrequencyInitBurstSeconds == null) {
            secondFallbackFrequencyInitBurstSeconds = PropertyUtils.getRemoteSyncLocalConfig("secondFallback.frequency.initBurstSeconds", Double.class, 200.0, cfg);
        }
        return secondFallbackFrequencyInitBurstSeconds;
    }

    /**
     * 二次降级熔断器打开状态持续最大时间(ms)，大于等级该时间将触发降级
     */
    public static long getSwitchOpenStatusDurationThreshold(Properties cfg) {
        if (secondFallbackDuration == null) {
            secondFallbackDuration = PropertyUtils.getRemoteSyncLocalConfig("secondFallback.duration", Long.class, 120000L, cfg);
        }
        return secondFallbackDuration;
    }

    /**
     * 从配置文件中构建性能指标
     *
     * @param configPrefix 配置前缀
     * @param cfg          配置
     * @return {@link List}<{@link PerformanceMetrics}> 性能指标列表
     */
    private static List<PerformanceMetrics> buildPerformanceMetrics(String configPrefix, Properties cfg) {
        List<PerformanceMetrics> performanceMetricsList = new ArrayList<>();
        for (MetricsKey each : MetricsKey.values()) {
            final Object metricsValue = PropertyUtils.getRemoteSyncLocalConfig(String.format("%s.%s", configPrefix, each.getKey()),
                    Object.class, null, cfg);
            final PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setMetricsKey(each);
            if (metricsValue instanceof String) {
                try {
                    final Object bean = GsonUtils.toObject((String) metricsValue, each.getValueType());
                    if (bean != null) {
                        metrics.setMetricsValue(bean);
                        performanceMetricsList.add(metrics);
                    }
                } catch (Exception e) {
                    DongTaiLog.warn("invalid metrics value config,msg:{}", e.getMessage());
                }
            }
        }
        return performanceMetricsList;
    }
}
