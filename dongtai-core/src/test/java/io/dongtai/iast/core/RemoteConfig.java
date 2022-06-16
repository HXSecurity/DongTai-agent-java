//package io.dongtai.iast.core;
//
//import com.google.gson.reflect.TypeToken;
//import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
//import io.dongtai.iast.common.entity.performance.metrics.CpuInfoMetrics;
//import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;
//import io.dongtai.iast.common.entity.performance.metrics.ThreadInfoMetrics;
//import io.dongtai.iast.common.entity.response.PlainResult;
//import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.FallbackSwitch;
//import io.dongtai.iast.core.utils.config.entity.RemoteConfigEntityV2;
//import io.dongtai.iast.core.utils.config.entity.PerformanceEntity;
//import io.dongtai.iast.core.utils.config.entity.PerformanceLimitThreshold;
//import io.dongtai.iast.core.utils.json.GsonUtils;
//import io.dongtai.iast.core.utils.threadlocal.BooleanThreadLocal;
//import io.dongtai.log.DongTaiLog;
//import org.json.JSONObject;
//import org.junit.Test;
//
//import java.util.List;
//
//public class RemoteConfig {
//
//    /**
//     * 全局配置
//     */
//    private static String existsRemoteConfigMeta = "{}";
//    private static Boolean enableAutoFallback;
//    /**
//     * 高频hook限流相关配置
//     */
//    private static Double hookLimitTokenPerSecond;
//    private static Double hookLimitInitBurstSeconds;
//    /**
//     * 高频流量限流相关配置
//     */
//    private static Double heavyTrafficLimitTokenPerSecond;
//    private static Double heavyTrafficLimitInitBurstSeconds;
//    private static Integer heavyTrafficBreakerWaitDuration;
//    /**
//     * 性能熔断阈值相关配置
//     */
//    private static Integer performanceBreakerWindowSize;
//    private static Double performanceBreakerFailureRate;
//    private static Integer performanceBreakerWaitDuration;
//    private static Integer performanceLimitRiskMaxMetricsCount;
//    private static List<PerformanceMetrics> performanceLimitRiskThreshold;
//    private static List<PerformanceMetrics> performanceLimitMaxThreshold;
//    /**
//     * 二次降级阈值相关配置
//     */
//    private static Double secondFallbackFrequencyTokenPerSecond;
//    private static Double secondFallbackFrequencyInitBurstSeconds;
//    private static Long secondFallbackDuration;
//
//
//    private static final String REMOTE_CONFIG_DEFAULT_META = "{}";
//    private static final String REMOTE_CONFIG_NEW_META = "{\"status\":201,\"msg\":\"\\u64cd\\u4f5c\\u6210\\u529f\",\"data\":{\"enableAutoFallback\":true,\"performanceLimitRiskMaxMetricsCount\":30,\"systemIsUninstall\":true,\"jvmIsUninstall\": true,\"applicationIsUninstall\": true,\"system\":[{\"fallbackName\":\"cpuUsagePercentage\",\"conditions\":\"greater\",\"value\":100,\"description\":\"系统 CPU 使用率阈值\"},{\"fallbackName\":\"sysMemUsagePercentage\",\"conditions\":\"greater\",\"value\":100,\"description\":\"系统内存使用率阈值\"},{\"fallbackName\":\"sysMemUsageUsed\",\"conditions\":\"greater\",\"value\":100000000000,\"description\":\"系统内存使用值阈值\"}],\"jvm\":[{\"fallbackName\":\"jvmMemUsagePercentage\",\"conditions\":\"greater\",\"value\":100,\"description\":\"JVM 内存使用率阈值\"},{\"fallbackName\":\"jvmMemUsageUsed\",\"conditions\":\"greater\",\"value\":100000000000,\"description\":\"JVM 内存使用值阈值\"},{\"fallbackName\":\"threadCount\",\"conditions\":\"greater\",\"value\":100000,\"description\":\"总线程数阈值\"},{\"fallbackName\":\"daemonThreadCount\",\"conditions\":\"greater\",\"value\":1000000,\"description\":\"守护线程数阈值\"},{\"fallbackName\":\"dongTaiThreadCount\",\"conditions\":\"greater\",\"value\":1000000,\"description\":\"洞态IAST线程数阈值\"}],\"appliaction\":[{\"fallbackName\":\"hookLimitTokenPerSecond\",\"conditions\":\"greater\",\"value\":10000,\"description\":\"单请求 HOOK 限流\"},{\"fallbackName\":\"heavyTrafficLimitTokenPerSecond\",\"conditions\":\"greater\",\"value\":100000000,\"description\":\"高频 HOOK 限流\"}]}}";
//
//    /**
//     * 解析远程配置响应
//     */
//    private static RemoteConfigEntityV2 parseRemoteConfigResponse(String remoteResponse) {
//        try {
//            // 默认响应标识调用失败
//            if (REMOTE_CONFIG_DEFAULT_META.equals(remoteResponse)) {
//                FallbackSwitch.setPerformanceFallback(false);
//                return null;
//            }
//            if (REMOTE_CONFIG_DEFAULT_META.equals(new JSONObject(remoteResponse).get("data"))){
//                FallbackSwitch.setPerformanceFallback(false);
//                return null;
//            }
//            PlainResult<RemoteConfigEntityV2> result = GsonUtils.toObject(remoteResponse, new TypeToken<PlainResult<RemoteConfigEntityV2>>() {
//            }.getType());
//            // 服务端响应成功状态码
//            if (result.isSuccess()) {
//                return result.getData();
//            } else {
//                DongTaiLog.warn("remoteConfig request not success, status:{}, msg:{},response:{}", result.getStatus(), result.getMsg(),
//                        GsonUtils.toJson(remoteResponse));
//                return null;
//            }
//        } catch (Throwable t) {
//            DongTaiLog.warn("remoteConfig parse failed: msg:{}, err:{}, response:{}", t.getMessage(), t.getCause(), GsonUtils.toJson(remoteResponse));
//            return null;
//        }
//    }
//
//    public void syncRemoteConfig() {
//        try {
//            // 远端有配置且和上次配置内容不一致时，重新更新配置文件
//            String remoteResponse = REMOTE_CONFIG_NEW_META;
//            RemoteConfigEntityV2 remoteConfigEntity = parseRemoteConfigResponse(remoteResponse);
//            List<PerformanceEntity> application = remoteConfigEntity.getApplication();
//            List<PerformanceEntity> jvm = remoteConfigEntity.getJvm();
//            List<PerformanceEntity> system = remoteConfigEntity.getSystem();
//            PerformanceLimitThreshold performanceLimitThreshold = new PerformanceLimitThreshold();
//            MemoryUsageMetrics memoryUsage = new MemoryUsageMetrics();
//            ThreadInfoMetrics threadInfoMetrics = new ThreadInfoMetrics();
//            CpuInfoMetrics cpuInfoMetrics = new CpuInfoMetrics();
//            MemoryUsageMetrics memoryNoHeapUsage = new MemoryUsageMetrics();
//
//
//            if (null != remoteConfigEntity && !remoteResponse.equals(existsRemoteConfigMeta)) {
//                if (remoteConfigEntity.getEnableAutoFallback() != null) {
//                    enableAutoFallback = remoteConfigEntity.getEnableAutoFallback();
//                }
//                if (remoteConfigEntity.getPerformanceLimitRiskMaxMetricsCount() != null) {
//                    performanceLimitRiskMaxMetricsCount = remoteConfigEntity.getPerformanceLimitRiskMaxMetricsCount()/30 + remoteConfigEntity.getPerformanceLimitRiskMaxMetricsCount()%30==0?0:1;
//                }
//
//                performanceLimitRiskMaxMetricsCount = remoteConfigEntity.getPerformanceLimitRiskMaxMetricsCount();
//                if (remoteConfigEntity.getApplication() != null) {
//                    for (PerformanceEntity performanceEntity:application){
//                        switch (performanceEntity.getFallbackName()){
//                            case "hookLimitTokenPerSecond":
//                                hookLimitTokenPerSecond = performanceEntity.getValue();
//                                break;
//                            case "heavyTrafficLimitTokenPerSecond":
//                                heavyTrafficLimitTokenPerSecond = performanceEntity.getValue();
//                                break;
//                        }
//                    }
//                }
//
//                if (remoteConfigEntity.getJvm() != null) {
//                    for (PerformanceEntity performanceEntity:jvm){
//                        switch (performanceEntity.getFallbackName()){
//                            case "jvmMemUsagePercentage":{
//                                memoryUsage.setMemUsagePercentage(performanceEntity.getValue());
//                                break;
//                            }
//                            case "jvmMemUsageUsed":{
//                                memoryUsage.setUsed(performanceEntity.getValue().longValue());
//                                break;
//                            }
//                            case "threadCount":{
//                                threadInfoMetrics.setThreadCount(performanceEntity.getValue().intValue());
//                                break;
//                            }
//                            case "daemonThreadCount":{
//                                threadInfoMetrics.setDaemonThreadCount(performanceEntity.getValue().intValue());
//                                break;
//                            }
//                            case "dongTaiThreadCount":{
//                                threadInfoMetrics.setDongTaiThreadCount(performanceEntity.getValue().intValue());
//                                break;
//                            }
//                        }
//                    }
//                }
//
//                if (remoteConfigEntity.getSystem() != null) {
//                    for (PerformanceEntity performanceEntity:system){
//                        switch (performanceEntity.getFallbackName()){
//                            case "cpuUsagePercentage":{
//                                cpuInfoMetrics.setCpuUsagePercentage(performanceEntity.getValue());
//                                break;
//                            }
//                            case "sysMemUsagePercentage":{
//                                memoryNoHeapUsage.setMemUsagePercentage(performanceEntity.getValue());
//                                break;
//                            }
//                            case "sysMemUsageUsed":{
//                                memoryNoHeapUsage.setUsed(performanceEntity.getValue().longValue());
//                                break;
//                            }
//                        }
//                    }
//                }
//
//                threadInfoMetrics.setPeakThreadCount(1000000000);
//                memoryUsage.setMax(1000000000000L);
//                memoryNoHeapUsage.setMax(1000000000000L);
//                performanceLimitThreshold.setThreadInfo(threadInfoMetrics);
//                performanceLimitThreshold.setMemoryUsage(memoryUsage);
//                performanceLimitThreshold.setMemoryNoHeapUsage(memoryNoHeapUsage);
//                performanceLimitThreshold.setCpuUsage(cpuInfoMetrics);
//                performanceLimitRiskThreshold = performanceLimitRiskThreshold;
//                existsRemoteConfigMeta = remoteResponse;
//                DongTaiLog.debug("Sync remote config successful.");
//            }
//        } catch (Throwable t) {
//            DongTaiLog.warn("Sync remote config failed, msg: {}, error: {}", t.getMessage(), t.getCause());
//        }
//    }
//
//    private static final BooleanThreadLocal HEAVY_HOOK_FALLBACK = new BooleanThreadLocal(false);
//
//    @Test
//    public void testJson(){
//        HEAVY_HOOK_FALLBACK.remove();
//        System.out.println(HEAVY_HOOK_FALLBACK.get());
//        HEAVY_HOOK_FALLBACK.remove();
//        System.out.println(HEAVY_HOOK_FALLBACK.get());
//        HEAVY_HOOK_FALLBACK.remove();
//        System.out.println(HEAVY_HOOK_FALLBACK.get());
//    }
//
//}
