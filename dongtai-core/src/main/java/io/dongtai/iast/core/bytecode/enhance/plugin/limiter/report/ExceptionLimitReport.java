package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import org.json.JSONObject;

/**
 * 异常限流-限制报告上报器
 *
 * @author liyuan40
 * @date 2022/3/7 16:36
 */
public class ExceptionLimitReport extends AbstractLimitReport {
    /**
     * 发送报告
     *
     * @param fallback                     异常限流器开关状态开关值
     * @param failureRateOnStateTransition 状态变化时的失败率
     * @param failureRateThreshold         失败率阈值
     */
    public static void sendReport(boolean fallback, double failureRateOnStateTransition, double failureRateThreshold) {
        String report = createReport(fallback, failureRateOnStateTransition, failureRateThreshold);
        sendReport(report);
    }

    protected static String createReport(boolean fallback, double failureRateOnEvent, double failureRateThreshold) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_LIMIT_EXCEPTION_RATE);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_ID, EngineManager.getAgentId());
        detail.put(ReportConstant.LIMIT_EXCEPTION_SWITCH_OPERATE, fallback);
        detail.put(ReportConstant.LIMIT_EXCEPTION_REAL_TIME_RATE, failureRateOnEvent);
        detail.put(ReportConstant.LIMIT_EXCEPTION_RATE_THRESHOLD, failureRateThreshold);

        return report.toString();
    }
}
