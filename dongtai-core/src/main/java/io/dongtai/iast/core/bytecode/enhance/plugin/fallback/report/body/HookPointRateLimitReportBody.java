package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.body;

import com.google.gson.annotations.SerializedName;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    @SerializedName(ReportConstant.REPORT_KEY)
    private Integer type = ReportConstant.REPORT_LIMIT_HOOK_POINT_RATE;
    /**
     * 报告详情
     */
    @SerializedName(ReportConstant.REPORT_VALUE_KEY)
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
        @SerializedName(ReportConstant.AGENT_ID)
        private Integer agentId;
        /**
         * 限流发生时间
         */
        @SerializedName(ReportConstant.LIMIT_HOOK_LIMIT_DATE)
        private Date limitDate;
        /**
         * 触发限流的类名
         */
        @SerializedName(ReportConstant.LIMIT_HOOK_POINT_CLASS_NAME)
        private String className;
        /**
         * 触发限流的方法
         */
        @SerializedName(ReportConstant.LIMIT_HOOK_POINT_METHOD)
        private String method;
        /**
         * 触发限流的方法签名
         */
        @SerializedName(ReportConstant.LIMIT_HOOK_POINT_METHOD_SIGN)
        private String methodSign;
        /**
         * hook点类型
         */
        @SerializedName(ReportConstant.LIMIT_HOOK_POINT_TYPE)
        private Integer hookType;
        /**
         * 限流速率
         */
        @SerializedName(ReportConstant.LIMIT_HOOK_POINT_RATE)
        private Double limitRate;

    }


}
