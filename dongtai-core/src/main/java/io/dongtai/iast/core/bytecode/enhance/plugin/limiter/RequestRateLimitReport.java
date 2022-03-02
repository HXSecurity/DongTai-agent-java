package io.dongtai.iast.core.bytecode.enhance.plugin.limiter;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.enums.RequestTypeEnum;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import org.json.JSONObject;

/**
 * 请求限速-限制报告上报器
 *
 * @author liyuan40
 * @date 2022/3/1 15:06
 */
public class RequestRateLimitReport extends AbstractLimitReport{
    public static void sendReport(RequestTypeEnum requestType, double rate) {
        String report = createReport(requestType, rate);
        sendReport(report);
    }

    protected static String createReport(RequestTypeEnum requestType, double rate) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_LIMIT_REQUEST_RATE);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_ID, EngineManager.getAgentId());
        detail.put(ReportConstant.LIMIT_REQUEST_TYPE, requestType.getType());
        detail.put(ReportConstant.LIMIT_REQUEST_RATE, rate);

        return report.toString();
    }
}
