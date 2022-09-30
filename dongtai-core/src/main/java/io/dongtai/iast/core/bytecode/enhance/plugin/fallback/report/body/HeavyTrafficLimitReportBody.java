package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.body;

import com.google.gson.annotations.SerializedName;
import io.dongtai.iast.common.constants.ReportKey;
import io.dongtai.iast.common.constants.ReportType;
import lombok.*;
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
    @SerializedName(ReportKey.TYPE)
    private Integer type = ReportType.LIMIT_HEAVY_TRAFFIC_RATE;
    /**
     * 报告详情
     */
    @SerializedName(ReportKey.DETAIL)
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
        @SerializedName(ReportKey.AGENT_ID)
        private Integer agentId;
        /**
         * 限流发生时间
         */
        @SerializedName("limitDate")
        private Date limitDate;
        /**
         * 限流阈值
         */
        @SerializedName("limitRate")
        private Double limitRate;

    }


}
