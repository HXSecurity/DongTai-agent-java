package io.dongtai.iast.agent.fallback;

import com.alibaba.fastjson2.JSONObject;
import com.google.gson.reflect.TypeToken;
import io.dongtai.iast.agent.IastProperties;
import io.dongtai.iast.agent.fallback.entity.*;
import io.dongtai.iast.agent.util.GsonUtils;
import io.dongtai.iast.agent.util.HttpClientUtils;
import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.CpuInfoMetrics;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;
import io.dongtai.iast.common.entity.response.PlainResult;
import io.dongtai.iast.common.enums.MetricsKey;
import io.dongtai.iast.common.state.AgentState;
import io.dongtai.iast.common.state.State;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;


import java.lang.reflect.Field;
import java.util.*;

/**
 * 远端配置工具类
 *
 * @author chenyi
 * @date 2022/3/3
 */
public class FallbackConfig {

    private static final String KEY_AGENT_ID = "agentId";
    private static final String REMOTE_CONFIG_DEFAULT_META = "{}";

    private FallbackConfig() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 全局配置
     */
    private static String existsRemoteConfigMeta = "{}";
    private static Boolean enableAutoFallback;
    /**
     * 性能熔断阈值相关配置
     */
    private static Integer performanceBreakerWindowSize;
    private static Double performanceBreakerFailureRate;
    private static Integer performanceBreakerWaitDuration;
    private static List<PerformanceMetrics> performanceLimitMaxThreshold;

    /**
     * 同步远程配置-v2
     *
     * @param agentId agent的唯一标识
     */
    public static void syncRemoteConfigV2(int agentId) {
        try {
            String remoteResponse = getConfigFromRemoteV2(agentId);
            // 远端有配置且和上次配置内容不一致时，重新更新配置文件
            FallbackConfigEntity remoteConfigEntity = parseRemoteConfigResponseV2(remoteResponse);
            if (null == remoteConfigEntity || existsRemoteConfigMeta.equals(remoteResponse)) {
                return;
            }
            List<PerformanceEntity> system = remoteConfigEntity.getSystem();
            PerformanceLimitThreshold performanceLimitThreshold = new PerformanceLimitThreshold();
            MemoryUsageMetrics memoryUsage = new MemoryUsageMetrics();
            CpuInfoMetrics cpuInfoMetrics = new CpuInfoMetrics();
            if (remoteConfigEntity.getEnableAutoFallback() != null) {
                enableAutoFallback = remoteConfigEntity.getEnableAutoFallback();
            }

            if (system != null) {
                for (PerformanceEntity performanceEntity : system) {
                    if ("cpuUsagePercentage".equals(performanceEntity.getFallbackName())) {
                        cpuInfoMetrics.setCpuUsagePercentage(performanceEntity.getValue());
                    } else if ("sysMemUsagePercentage".equals(performanceEntity.getFallbackName())) {
                        memoryUsage.setMemUsagePercentage(performanceEntity.getValue());
                    } else if ("sysMemUsageUsed".equals(performanceEntity.getFallbackName())) {
                        memoryUsage.setUsed(performanceEntity.getValue().longValue());
                    }
                }
            }

            memoryUsage.setMax(1000000000000L);
            performanceLimitThreshold.setMemoryUsage(memoryUsage);
            performanceLimitThreshold.setCpuUsage(cpuInfoMetrics);
            performanceLimitMaxThreshold = combineRemoteAndLocalMetricsThreshold(performanceLimitMaxThreshold,
                    performanceLimitThreshold);
            existsRemoteConfigMeta = remoteResponse;
            DongTaiLog.debug("Sync remote fallback config successfully");
        } catch (Throwable t) {
            DongTaiLog.warn(ErrorCode.AGENT_FALLBACK_SYNC_REMOTE_CONFIG_FAILED, t.getMessage(), t.getCause().getMessage());
        }
    }

    /**
     * 根据agentID获取服务端对Agent的配置
     */
    private static String getConfigFromRemoteV2(int agentId) {
        JSONObject report = new JSONObject();
        report.put(KEY_AGENT_ID, agentId);
        try {
            StringBuilder response = HttpClientUtils.sendPost(ApiPath.SERVER_CONFIG_V2, report.toString());
            return response.toString();
        } catch (Throwable t) {
            return REMOTE_CONFIG_DEFAULT_META;
        }
    }

    private static FallbackConfigEntity parseRemoteConfigResponseV2(String remoteResponse) {
        try {
            // 默认响应标识调用失败
            if (REMOTE_CONFIG_DEFAULT_META.equals(remoteResponse)
                    || REMOTE_CONFIG_DEFAULT_META.equals(JSONObject.parseObject(remoteResponse).get("data").toString())) {
                FallbackConfig.enableAutoFallback = false;
                if (AgentState.getInstance().isFallback()) {
                    DongTaiLog.info("fallback remote config empty, auto fallback closed, starting agent");
                    FallbackSwitch.setPerformanceFallback(State.RUNNING);
                }
                return null;
            }
            PlainResult<FallbackConfigEntity> result = GsonUtils.toObject(remoteResponse, new TypeToken<PlainResult<FallbackConfigEntity>>() {
            }.getType());
            // 服务端响应成功状态码
            if (result.isSuccess()) {
                return result.getData();
            } else {
                DongTaiLog.debug("remoteConfig request not success, status:{}, msg:{},response:{}", result.getStatus(), result.getMsg(),
                        GsonUtils.toJson(remoteResponse));
                return null;
            }
        } catch (Throwable t) {
            DongTaiLog.debug("remoteConfig parse failed: msg:{}, err:{}, response:{}", t.getMessage(), t.getCause(), GsonUtils.toJson(remoteResponse));
            return null;
        }
    }


    /**
     * 合并远端和本地的指标阈值配置
     *
     * @param localThreshold  本地阈值配置
     * @param remoteThreshold 远端阈值配置
     * @return {@link List}<{@link PerformanceMetrics}>
     */
    private static List<PerformanceMetrics> combineRemoteAndLocalMetricsThreshold(List<PerformanceMetrics> localThreshold,
                                                                                  PerformanceLimitThreshold remoteThreshold) {
        List<PerformanceMetrics> performanceMetricsList = new ArrayList<PerformanceMetrics>();
        for (MetricsKey metricsKey : MetricsKey.values()) {
            //远端包含该指标配置
            if (remoteThreshold != null) {
                Object metricsValue = getMetricsFromPerformanceLimitThreshold(remoteThreshold, metricsKey);
                if (metricsValue != null) {
                    PerformanceMetrics metrics = new PerformanceMetrics();
                    metrics.setMetricsKey(metricsKey);
                    metrics.setMetricsValue(metricsValue);
                    performanceMetricsList.add(metrics);
                    continue;
                }
            }
            //本地包含该指标配置
            if (localThreshold != null) {
                for (PerformanceMetrics each : localThreshold) {
                    if (each.getMetricsKey() == metricsKey) {
                        performanceMetricsList.add(each);
                        break;
                    }
                }
            }
        }
        return performanceMetricsList;
    }

    /**
     * 获取性能限制阈值配置中，与指标名称相同的指标度量值
     *
     * @param threshold  阈值配置
     * @param metricsKey 指标名称
     * @return {@link Object}
     */
    private static Object getMetricsFromPerformanceLimitThreshold(PerformanceLimitThreshold threshold, MetricsKey metricsKey) {
        try {
            final Field field = threshold.getClass().getDeclaredField(metricsKey.getKey());
            field.setAccessible(true);
            return field.get(threshold);
        } catch (Throwable e) {
            return null;
        }
    }

    // *************************************************************
    // 全局配置(本地)
    // *************************************************************

    /**
     * 是否允许自动降级
     */
    public static Boolean enableAutoFallback() {
        if (enableAutoFallback == null) {
            enableAutoFallback = IastProperties.getInstance().getRemoteFallbackConfig(
                    "global.autoFallback", Boolean.class, false);
        }
        return enableAutoFallback;
    }

    // *************************************************************
    // 性能熔断阈值相关配置(本地)
    // *************************************************************

    /**
     * 性能熔断-统计窗口大小
     */
    public static Integer getPerformanceBreakerWindowSize(Properties cfg) {
        if (performanceBreakerWindowSize == null) {
            performanceBreakerWindowSize = IastProperties.getInstance().getRemoteFallbackConfig(
                    "performanceLimit.performanceBreakerWindowSize", Integer.class, 2, cfg);
        }
        return performanceBreakerWindowSize;
    }

    public static void setPerformanceBreakerWindowSize(Integer performanceBreakerWindowSize) {
        FallbackConfig.performanceBreakerWindowSize = performanceBreakerWindowSize;
    }

    /**
     * 性能熔断-失败率阈值
     */
    public static Double getPerformanceBreakerFailureRate(Properties cfg) {
        if (performanceBreakerFailureRate == null) {
            performanceBreakerFailureRate = IastProperties.getInstance().getRemoteFallbackConfig(
                    "performanceLimit.performanceBreakerFailureRate", Double.class, 51.0, cfg);
        }
        return performanceBreakerFailureRate;
    }

    public static void setPerformanceBreakerFailureRate(Double performanceBreakerFailureRate) {
        FallbackConfig.performanceBreakerFailureRate = performanceBreakerFailureRate;
    }

    /**
     * 性能熔断-自动转半开的等待时间(单位:秒)
     */
    public static Integer getPerformanceBreakerWaitDuration(Properties cfg) {
        if (performanceBreakerWaitDuration == null) {
            performanceBreakerWaitDuration = IastProperties.getInstance().getRemoteFallbackConfig(
                    "performanceLimit.performanceBreakerWaitDuration", Integer.class, 40, cfg);
        }
        return performanceBreakerWaitDuration;
    }

    public static void setPerformanceBreakerWaitDuration(Integer performanceBreakerWaitDuration) {
        FallbackConfig.performanceBreakerWaitDuration = performanceBreakerWaitDuration;
    }

    /**
     * 性能熔断-最大阈值配置
     */
    public static List<PerformanceMetrics> getPerformanceLimitMaxThreshold(Properties cfg) {
        if (performanceLimitMaxThreshold == null) {
            performanceLimitMaxThreshold = buildPerformanceMetrics("performanceLimit.maxThreshold", cfg);
        }
        return performanceLimitMaxThreshold;
    }

    /**
     * 从配置文件中构建性能指标
     *
     * @param configPrefix 配置前缀
     * @param cfg          配置
     * @return {@link List}<{@link PerformanceMetrics}> 性能指标列表
     */
    private static List<PerformanceMetrics> buildPerformanceMetrics(String configPrefix, Properties cfg) {
        List<PerformanceMetrics> performanceMetricsList = new ArrayList<PerformanceMetrics>();
        for (MetricsKey each : MetricsKey.values()) {
            final Object metricsValue = IastProperties.getInstance().getRemoteFallbackConfig(
                    String.format("%s.%s", configPrefix, each.getKey()),
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
                } catch (Throwable e) {
                    DongTaiLog.warn(ErrorCode.AGENT_FALLBACK_METRICS_CONFIG_INVALID, metricsValue, e.getMessage());
                }
            }
        }
        return performanceMetricsList;
    }
}
