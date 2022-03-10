package io.dongtai.iast.core.utils;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.core.utils.json.GsonUtils;

import java.util.ArrayList;
import java.util.List;

public class ServerConfigBody {
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


    public static void main(String[] args) {
        String json = "{\n" +
                "  \"enableAutoFallback\": false,\n" +
                "  \"hookLimitTokenPerSecond\": 0.00,\n" +
                "  \"hookLimitInitBurstSeconds\": 0.00,\n" +
                "  \"performanceBreakerWindowSize\": 0,\n" +
                "  \"performanceBreakerFailureRate\": 0.00,\n" +
                "  \"performanceBreakerWaitDuration\": 0,\n" +
                "  \"maxRiskMetricsCount\": 0,\n" +
                "  \"performanceLimitRiskThreshold\": [\n" +
                "    {\n" +
                "      \"metricsKey\": \"CPU_USAGE\",\n" +
                "      \"metricsValue\": {\"cpuUsagePercentage\":10}\n" +
                "    }\n" +
                "  ],\n" +
                "  \"performanceLimitMaxThreshold\": [\n" +
                "    {\n" +
                "      \"metricsKey\": \"CPU_USAGE\",\n" +
                "      \"metricsValue\": {\"cpuUsagePercentage\":10}\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        ServerConfigBody serverConfigBody = GsonUtils.toObject(json,ServerConfigBody.class);
        System.out.println(serverConfigBody.toString());

    }

    @Override
    public String toString() {
        return "ServerConfigBody{" +
                "enableAutoFallback=" + enableAutoFallback +
                ", hookLimitTokenPerSecond=" + hookLimitTokenPerSecond +
                ", hookLimitInitBurstSeconds=" + hookLimitInitBurstSeconds +
                ", performanceBreakerWindowSize=" + performanceBreakerWindowSize +
                ", performanceBreakerFailureRate=" + performanceBreakerFailureRate +
                ", performanceBreakerWaitDuration=" + performanceBreakerWaitDuration +
                ", maxRiskMetricsCount=" + maxRiskMetricsCount +
                ", performanceLimitRiskThreshold=" + performanceLimitRiskThreshold +
                ", performanceLimitMaxThreshold=" + performanceLimitMaxThreshold +
                '}';
    }
}
