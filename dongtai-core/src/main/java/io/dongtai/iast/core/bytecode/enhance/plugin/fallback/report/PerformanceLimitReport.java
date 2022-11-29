package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.body.PerformanceBreakReportBody;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

import java.util.Date;


/**
 * 性能熔断-限制报告上报器
 *
 * @author chenyi
 * @date 2022/3/7
 */
public class PerformanceLimitReport extends AbstractLimitReport {
    private static final PerformanceBreakReportBody PERFORMANCE_BREAK_REPORT_BODY = new PerformanceBreakReportBody();

    public static void appendPerformanceBreakLog(PerformanceBreakReportBody.PerformanceOverThresholdLog performanceOverThresholdLog) {
        PERFORMANCE_BREAK_REPORT_BODY.appendPerformanceBreakLog(performanceOverThresholdLog);
    }

    public static void sendReport() {
        try {
            PERFORMANCE_BREAK_REPORT_BODY.getDetail().setAgentId(EngineManager.getAgentId());
            PERFORMANCE_BREAK_REPORT_BODY.getDetail().setBreakDate(new Date());
            PERFORMANCE_BREAK_REPORT_BODY.clearAllPerformanceBreakLog();
        } catch (Throwable e) {
            DongTaiLog.error("sendReport failed. report: PerformanceLimitReport, reason: {}", e);
        }
    }

}
