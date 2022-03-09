package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.body.HeavyTrafficLimitReportBody;
import io.dongtai.iast.core.utils.json.GsonUtils;

import java.util.Date;

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
        final HeavyTrafficLimitReportBody reportBody = new HeavyTrafficLimitReportBody();
        reportBody.setDetail(new HeavyTrafficLimitReportBody.HeavyTrafficLimitDetail()
                .setAgentId(EngineManager.getAgentId())
                .setLimitDate(new Date())
                .setLimitRate(failureRateThreshold)
        );
        return GsonUtils.toJson(reportBody);
    }
}
