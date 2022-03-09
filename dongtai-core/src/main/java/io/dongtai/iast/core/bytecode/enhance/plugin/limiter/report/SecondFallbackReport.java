package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report;

import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.body.SecondFallbackReportBody;
import io.dongtai.iast.core.utils.json.GsonUtils;

import java.util.LinkedList;

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
     * @param secondFallbackReportDetailLog 报告内容
     */
    public static void sendReport(LinkedList<SecondFallbackReportBody.AbstractSecondFallbackReportLog> secondFallbackReportDetailLog) {
        SecondFallbackReportBody reportBody = new SecondFallbackReportBody(secondFallbackReportDetailLog);
        sendReport(GsonUtils.toJson(reportBody));
    }
}
