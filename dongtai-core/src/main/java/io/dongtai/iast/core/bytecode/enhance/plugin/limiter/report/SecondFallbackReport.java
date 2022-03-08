package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import org.json.JSONObject;

/**
 * 二次降级发送报告
 *
 * @author liyuan40
 * @date 2022/3/8 16:50
 */
public class SecondFallbackReport extends AbstractLimitReport {
    /**
     * 发送报告
     *
     * @param report 报告
     */
    public static void sendSecondFallbackReport(JSONObject report) {
        sendReport(createReport(report));
    }

    protected static String createReport(JSONObject reportJson) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_SECOND_FALLBACK);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_ID, EngineManager.getAgentId());
        detail.put(ReportConstant.SECOND_FALLBACK_REPORT_INFO, reportJson);

        return report.toString();
    }
}
