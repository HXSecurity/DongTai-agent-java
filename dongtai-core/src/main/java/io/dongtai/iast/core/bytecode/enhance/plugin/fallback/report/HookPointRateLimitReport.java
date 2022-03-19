package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.body.HookPointRateLimitReportBody;
import io.dongtai.iast.core.utils.json.GsonUtils;

import java.util.Date;


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
        final HookPointRateLimitReportBody reportBody = new HookPointRateLimitReportBody();
        reportBody.setDetail(new HookPointRateLimitReportBody.HookPointRateLimitDetail()
                .setAgentId(EngineManager.getAgentId())
                .setLimitDate(new Date())
                .setClassName(className)
                .setMethod(method)
                .setMethodSign(methodSign)
                .setHookType(hookType)
                .setLimitRate(rate)
        );
        return GsonUtils.toJson(reportBody);
    }

}
