package io.dongtai.iast.agent.fallback.checker;

import io.dongtai.iast.agent.IastProperties;
import io.dongtai.iast.agent.fallback.checker.impl.MemUsageChecker;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;
import io.dongtai.iast.common.enums.MetricsKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class MemUsageCheckerTest {


    /**
     * 测试内存检查
     */
    @Test
    public void testIsPerformanceOverLimit() {
        // 配置参数
        Properties cfg = new Properties();
        // 设置配置内存阈值
        cfg.setProperty("iast.remoteSync.performanceLimit.maxThreshold.memoryUsage", "{\"committed\":1024," +
                "\"init\":1024,\"max\":1024,\"memUsagePercentage\":80.0,\"systemMaxLimit\":-1," +
                "\"trulyMaxMem\":1024,\"used\":1024}\n");
        //初始化临时目录
        IastProperties.initTmpDir();
        // 创建检查器对象
        MemUsageChecker memUsageChecker = new MemUsageChecker();
        // 创建模拟性能指标对象
        PerformanceMetrics nowMetrics = new PerformanceMetrics();
        nowMetrics.setMetricsKey(MetricsKey.MEM_USAGE);
        nowMetrics.setMetricsValue(new MemoryUsageMetrics(1024L, 1024L, 1024L, 1024L));
        // 内存使用率超过阈值，应该返回true
        Assert.assertTrue(memUsageChecker.isPerformanceOverLimit(nowMetrics, cfg));


        // 修改性能指标对象的内存使用率为70%
        nowMetrics.setMetricsValue(new MemoryUsageMetrics(1024L, 500L, 1024L, 1024L));
        // 内存使用率未超过阈值，应该返回false
        Assert.assertFalse(memUsageChecker.isPerformanceOverLimit(nowMetrics, cfg));
    }
}
