package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report;

import io.dongtai.iast.common.utils.FixSizeLinkedList;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.body.SecondFallbackReportBody;
import io.dongtai.iast.core.utils.json.GsonUtils;

/**
 * 二次降级发送报告
 *
 * @author liyuan40
 * @date 2022/3/8 16:50
 */
public class SecondFallbackReport extends AbstractLimitReport {

    private static final SecondFallbackReportBody REPORT = new SecondFallbackReportBody(new FixSizeLinkedList<>(30));

    /**
     * 检查报告
     */
    public static boolean checkReport() {
        if (REPORT.isEmpty()) {
            return false;
        } else {
            sendReport(GsonUtils.toJson(REPORT));
            REPORT.clear();
            return true;
        }
    }

    /**
     * 添加日志报告
     *
     * @param log 日志
     */
    public static void addReportLog(SecondFallbackReportBody.AbstractSecondFallbackReportLog log) {
        REPORT.addSecondFallbackReportLog(log);
    }

}
