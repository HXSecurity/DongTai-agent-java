package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.body.HeavyTrafficLimitReportBody;
import io.dongtai.iast.core.utils.json.GsonUtils;

import java.util.Date;

/**
 * 高频流量限速-限制报告上报器
 *
 * @author liyuan40
 * @date 2022/3/1 15:06
 */
public class HeavyTrafficRateLimitReport extends AbstractLimitReport {
    /**
     * 发送请求限速报告
     *
     * @param trafficLimitRate qps 阈值
     */
    public static void sendReport(double trafficLimitRate) {
        String report = createReport(trafficLimitRate);
        sendReport(report);
    }

    protected static String createReport(double trafficLimitRate) {
        final HeavyTrafficLimitReportBody reportBody = new HeavyTrafficLimitReportBody();
        reportBody.setDetail(new HeavyTrafficLimitReportBody.HeavyTrafficLimitDetail()
                .setAgentId(EngineManager.getAgentId())
                .setLimitDate(new Date())
                .setLimitRate(trafficLimitRate)
        );
        return GsonUtils.toJson(reportBody);
    }
}
