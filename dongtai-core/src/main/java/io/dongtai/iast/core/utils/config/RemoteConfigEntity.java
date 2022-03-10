package io.dongtai.iast.core.utils.config;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.core.utils.json.GsonUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 远程配置实体
 *
 * @author me
 * @date 2022/03/10
 */
@Data
public class RemoteConfigEntity {

    // *************************************************************
    // 全局配置
    // *************************************************************

    /**
     * 是否允许自动降级
     */
    @SerializedName("enableAutoFallback")
    private Boolean enableAutoFallback;

    // *************************************************************
    // 高频hook限流相关配置
    // *************************************************************
    /**
     * 高频hook限流-每秒获得令牌数
     */
    @SerializedName("hookLimitTokenPerSecond")
    private Double hookLimitTokenPerSecond;
    /**
     * 高频hook限流-初始预放置令牌时间
     */
    @SerializedName("hookLimitInitBurstSeconds")
    private Double hookLimitInitBurstSeconds;

    // *************************************************************
    // 性能熔断阈值相关配置
    // *************************************************************

    /**
     * 性能断路器-统计窗口大小
     */
    @SerializedName("performanceBreakerWindowSize")
    private Integer performanceBreakerWindowSize;
    /**
     * 性能断路器-失败率阈值
     */
    @SerializedName("performanceBreakerFailureRate")
    private Double performanceBreakerFailureRate;
    /**
     * 性能断路器-自动转半开的等待时间(单位:秒)
     */
    @SerializedName("performanceBreakerWaitDuration")
    private Integer performanceBreakerWaitDuration;
    /**
     * 性能断路器-不允许超过风险阈值的指标数量(0为不限制，达到阈值数时熔断)
     */
    @SerializedName("maxRiskMetricsCount")
    private Integer maxRiskMetricsCount;
    /**
     * 性能断路器-风险阈值配置
     */
    @SerializedName("performanceLimitRiskThreshold")
    private List<PerformanceMetrics> performanceLimitRiskThreshold;
    /**
     * 性能断路器-最大阈值配置
     */
    @SerializedName("performanceLimitMaxThreshold")
    private List<PerformanceMetrics> performanceLimitMaxThreshold;

    public List<PerformanceMetrics> getPerformanceLimitRiskThreshold() {
        return convert2PerformanceMetricsList(performanceLimitRiskThreshold);
    }

    /**
     * Gson无法将转json转化为List<PerformanceMetrics>，需要特殊处理
     */
    private List<PerformanceMetrics> convert2PerformanceMetricsList(List<PerformanceMetrics> sourceList) {
        List<PerformanceMetrics> metricsList = new ArrayList<>();
        if (sourceList != null) {
            for (PerformanceMetrics metrics : sourceList) {
                if (metrics != null) {
                    metrics.setMetricsValue(convert2TrulyTypeObj(metrics.getMetricsValue(), metrics.getMetricsKey().getValueType()));
                    metricsList.add(metrics);
                }
            }
        }
        return metricsList;
    }

    private <T> T convert2TrulyTypeObj(Object each, Class<T> type) {
        if (type.isInstance(each)) {
            return type.cast(each);
        } else if (each instanceof LinkedTreeMap) {
            LinkedTreeMap eachMap = (LinkedTreeMap) each;
            return GsonUtils.toObject(GsonUtils.toJson(eachMap), type);
        } else {
            return null;
        }
    }
}
