package io.dongtai.iast.core.bytecode.enhance.plugin.limiter;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import org.json.JSONObject;


/**
 * hook点限速-限制报告上报器
 *
 * @author chenyi
 * @date 2022/2/28
 */
public class HookPointRateLimitReport extends AbstractLimitReport {

    public static void sendReport(String className, String method, String methodSign, Integer hookType, Double rate) {
        String report = createReport(className, method, methodSign, hookType, rate);
        sendReport(report);
    }

    protected static String createReport(String className, String method, String methodSign, Integer hookType, Double rate) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_LIMIT_HOOK_POINT_RATE);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_ID, EngineManager.getAgentId());
        detail.put(ReportConstant.LIMIT_HOOK_POINT_CLASS_NAME, className);
        detail.put(ReportConstant.LIMIT_HOOK_POINT_METHOD, method);
        detail.put(ReportConstant.LIMIT_HOOK_POINT_METHOD_SIGN, methodSign);
        detail.put(ReportConstant.LIMIT_HOOK_POINT_TYPE, hookType);
        detail.put(ReportConstant.LIMIT_HOOK_POINT_RATE, rate);

        return report.toString();
    }

}
