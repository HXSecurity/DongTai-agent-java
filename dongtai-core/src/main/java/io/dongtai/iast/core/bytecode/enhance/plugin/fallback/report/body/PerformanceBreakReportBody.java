package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.body;

import com.google.gson.annotations.SerializedName;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.utils.FixSizeLinkedList;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import lombok.Data;

import java.util.Date;
import java.util.LinkedList;

/**
 * 性能熔断日志体
 *
 * @author chenyi
 * @date 2022/3/7
 */
@Data
public class PerformanceBreakReportBody {

    /**
     * 报告类型
     */
    @SerializedName(ReportConstant.REPORT_KEY)
    private Integer type = ReportConstant.REPORT_LIMIT_PERFORMANCE_FALLBACK;

    /**
     * 报告详情
     */
    @SerializedName(ReportConstant.REPORT_VALUE_KEY)
    private PerformanceBreakDetail detail = new PerformanceBreakDetail();

    public void appendPerformanceBreakLog(PerformanceOverThresholdLog performanceOverThresholdLog) {
        detail.getPerformanceOverThresholdLog().add(performanceOverThresholdLog);
    }

    public void clearAllPerformanceBreakLog() {
        detail.getPerformanceOverThresholdLog().clear();
    }


    /**
     * 报告详情对象
     *
     * @author chenyi
     * @date 2022/03/07
     */
    @Data
    public static class PerformanceBreakDetail {
        /**
         * agentId
         */
        @SerializedName(ReportConstant.AGENT_ID)
        private Integer agentId;
        /**
         * 熔断时间
         */
        @SerializedName(ReportConstant.LIMIT_PERFORMANCE_BREAK_DATE)
        private Date breakDate;
        /**
         * 熔断前超限日志(最多保留30条)
         */
        @SerializedName(ReportConstant.LIMIT_PERFORMANCE_OVER_THRESHOLD_LOG)
        private LinkedList<PerformanceOverThresholdLog> performanceOverThresholdLog = new FixSizeLinkedList<>(30);

    }

    /**
     * 性能超限日志
     *
     * @author chenyi
     * @date 2022/03/07
     */
    @Data
    public static class PerformanceOverThresholdLog {
        /**
         * 记录时间
         */
        private Date date;
        /**
         * 超限类型(1:风险阈值,2:最大阈值)
         */
        private Integer overThresholdType;
        /**
         * 当前指标
         */
        private PerformanceMetrics nowMetrics;
        /**
         * 阈值指标
         */
        private PerformanceMetrics threshold;
        /**
         * 性能超限的指标数
         */
        private Integer overThresholdCount;

    }

}
