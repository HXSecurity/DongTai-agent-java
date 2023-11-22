package io.dongtai.iast.agent.fallback.checker;

import io.dongtai.iast.agent.IastProperties;
import io.dongtai.iast.agent.fallback.checker.impl.CpuUsageChecker;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.CpuInfoMetrics;
import io.dongtai.iast.common.enums.MetricsKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * 检查CPU使用率
 */
public class CpuUsageCheckerTest {

    @Test
    public void testIsPerformanceOverLimit() {


        // 创建配置参数对象
        Properties cfg = new Properties();
        cfg.setProperty("iast.remoteSync.performanceLimit.maxThreshold.cpuUsage", "{\"cpuUsagePercentage\":80.0}");

        //初始化临时目录
        IastProperties.initTmpDir();

        // 创建测试用例对象
        CpuUsageChecker cpuUsageChecker = new CpuUsageChecker();
        // 创建模拟性能指标对象
        PerformanceMetrics nowMetrics = new PerformanceMetrics();
        CpuInfoMetrics cpuInfoMetrics = new CpuInfoMetrics();
        cpuInfoMetrics.setCpuUsagePercentage(90.0);

        nowMetrics.setMetricsKey(MetricsKey.CPU_USAGE);
        nowMetrics.setMetricsValue(cpuInfoMetrics);

        // CPU使用率超过阈值，应该返回true
//        Assert.assertTrue(cpuUsageChecker.isPerformanceOverLimit(nowMetrics, cfg));

        // 修改性能指标对象的CPU使用率为70%
        cpuInfoMetrics.setCpuUsagePercentage(70.0);
        // CPU使用率未超过阈值，应该返回false
        Assert.assertFalse(cpuUsageChecker.isPerformanceOverLimit(nowMetrics, cfg));
    }
}
