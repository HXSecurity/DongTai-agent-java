package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report;

import io.dongtai.iast.common.utils.FixSizeLinkedList;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.body.SecondFallbackReportBody;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

/**
 * 二次降级发送报告
 *
 * @author liyuan40
 * @date 2022/3/8 16:50
 */
public class SecondFallbackReport extends AbstractLimitReport {

    private static final SecondFallbackReportBody FALLBACK_REPORT_LOG = new SecondFallbackReportBody(new FixSizeLinkedList<SecondFallbackReportBody.AbstractSecondFallbackReportLog>(30));

    /**
     * 二次降级日志是否为空
     */
    public static boolean isSecondFallbackLogEmpty() {
        return FALLBACK_REPORT_LOG.isEmpty();
    }

    /**
     * 发送报告
     */
    public static void sendReport(){
        try {
            FALLBACK_REPORT_LOG.getDetail().setAgentId(EngineManager.getAgentId());
//        sendReport(GsonUtils.toJson(FALLBACK_REPORT_LOG));
            sendReport(new JSONObject(FALLBACK_REPORT_LOG).toString());
            FALLBACK_REPORT_LOG.clear();
        } catch (Throwable e) {
            DongTaiLog.error("sendReport failed. report: SecondFallbackReport, reason: {}", e);
        }
    }

    /**
     * 追加二次降级日志
     *
     * @param log 日志
     */
    public static void appendLog(SecondFallbackReportBody.AbstractSecondFallbackReportLog log) {
        FALLBACK_REPORT_LOG.addSecondFallbackReportLog(log);
    }

}
