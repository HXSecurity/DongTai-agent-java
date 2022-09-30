package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.body;

import com.google.gson.annotations.SerializedName;
import io.dongtai.iast.common.constants.ReportKey;
import io.dongtai.iast.common.constants.ReportType;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 高频hook点限流日志体
 *
 * @author chenyi
 * @date 2022/03/09
 */
@Data
public class HookPointRateLimitReportBody {
    /**
     * 报告类型
     */
    @SerializedName(ReportKey.TYPE)
    private Integer type = ReportType.LIMIT_HOOK_POINT_RATE;
    /**
     * 报告详情
     */
    @SerializedName(ReportKey.DETAIL)
    private HookPointRateLimitDetail detail = new HookPointRateLimitDetail();

    /**
     * 高频hook点限流详情
     *
     * @author chenyi
     * @date 2022/03/09
     */
    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HookPointRateLimitDetail {
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
         * 触发限流的类名
         */
        @SerializedName("className")
        private String className;
        /**
         * 触发限流的方法
         */
        @SerializedName("method")
        private String method;
        /**
         * 触发限流的方法签名
         */
        @SerializedName("methodSign")
        private String methodSign;
        /**
         * hook点类型
         */
        @SerializedName("hookType")
        private Integer hookType;
        /**
         * 限流速率
         */
        @SerializedName("limitRate")
        private Double limitRate;

    }


}
