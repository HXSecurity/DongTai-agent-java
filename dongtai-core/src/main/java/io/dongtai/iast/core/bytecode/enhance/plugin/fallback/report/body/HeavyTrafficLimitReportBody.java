package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.body;

import com.google.gson.annotations.SerializedName;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 高频流量限流日志体
 *
 * @author chenyi
 * @date 2022/03/09
 */
@Data
public class HeavyTrafficLimitReportBody {
    /**
     * 报告类型
     */
    @SerializedName(ReportConstant.REPORT_KEY)
    private Integer type = ReportConstant.REPORT_LIMIT_HEAVY_TRAFFIC_RATE;
    /**
     * 报告详情
     */
    @SerializedName(ReportConstant.REPORT_VALUE_KEY)
    private HeavyTrafficLimitDetail detail = new HeavyTrafficLimitDetail();

    /**
     * 高频流量限流详情
     *
     * @author chenyi
     * @date 2022/03/09
     */
    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HeavyTrafficLimitDetail {
        /**
         * agentId
         */
        @SerializedName(ReportConstant.AGENT_ID)
        private Integer agentId;
        /**
         * 限流发生时间
         */
        @SerializedName(ReportConstant.LIMIT_TRAFFIC_OCCUR_DATE)
        private Date limitDate;
        /**
         * 限流阈值
         */
        @SerializedName(ReportConstant.LIMIT_TRAFFIC_RATE)
        private Double limitRate;

    }


}
