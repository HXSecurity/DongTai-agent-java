package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import org.json.JSONObject;

/**
 * 请求限速-限制报告上报器
 *
 * @author liyuan40
 * @date 2022/3/1 15:06
 */
public class RequestRateLimitReport extends AbstractLimitReport {
    /**
     * 发送请求限速报告
     *
     * @param fallback                     请求限流器开关状态开关值
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
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_LIMIT_REQUEST_RATE);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_ID, EngineManager.getAgentId());
        detail.put(ReportConstant.LIMIT_REQUEST_SWITCH_OPERATE, fallback);
        detail.put(ReportConstant.LIMIT_REQUEST_REAL_TIME_RATE, failureRateOnEvent);
        detail.put(ReportConstant.LIMIT_REQUEST_RATE_THRESHOLD, failureRateThreshold);

        return report.toString();
    }
}
