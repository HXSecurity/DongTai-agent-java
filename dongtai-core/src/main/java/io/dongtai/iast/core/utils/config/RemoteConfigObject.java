package io.dongtai.iast.core.utils.config;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.core.utils.json.GsonUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RemoteConfigObject {
    @SerializedName("enableAutoFallback")
    private Boolean enableAutoFallback;
    /**
     * 高频hook限流相关配置
     */
    @SerializedName("hookLimitTokenPerSecond")
    private Double hookLimitTokenPerSecond;
    @SerializedName("hookLimitInitBurstSeconds")
    private Double hookLimitInitBurstSeconds;
    /**
     * 性能熔断阈值相关配置
     */
    @SerializedName("performanceBreakerWindowSize")
    private Integer performanceBreakerWindowSize;
    @SerializedName("performanceBreakerFailureRate")
    private Double performanceBreakerFailureRate;
    @SerializedName("performanceBreakerWaitDuration")
    private Integer performanceBreakerWaitDuration;
    @SerializedName("maxRiskMetricsCount")
    private Integer maxRiskMetricsCount;
    @SerializedName("performanceLimitRiskThreshold")
    private List<PerformanceMetrics> performanceLimitRiskThreshold;
    @SerializedName("performanceLimitMaxThreshold")
    private List<PerformanceMetrics> performanceLimitMaxThreshold;

    public List<PerformanceMetrics> getPerformanceLimitRiskThreshold() {
        return convert2PerformanceMetricsList(performanceLimitRiskThreshold);
    }

    /**
     * Gson无法将转json转化为List<PerformanceMetrics>，需要特殊处理
     * @param sourceList
     * @return
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
