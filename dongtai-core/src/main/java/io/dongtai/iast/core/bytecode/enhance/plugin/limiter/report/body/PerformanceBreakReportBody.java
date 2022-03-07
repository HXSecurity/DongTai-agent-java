package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.body;

import com.google.gson.annotations.SerializedName;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 性能熔断日志体
 *
 * @author chenyi
 * @date 2022/3/7
 */
public class PerformanceBreakReportBody {

    /**
     * 报告类型
     */
    @SerializedName(ReportConstant.REPORT_KEY)
    private Integer reportKey = ReportConstant.REPORT_LIMIT_PERFORMANCE_FALLBACK;

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

    public Integer getReportKey() {
        return reportKey;
    }

    public void setReportKey(Integer reportKey) {
        this.reportKey = reportKey;
    }

    public PerformanceBreakDetail getDetail() {
        return detail;
    }

    public void setDetail(PerformanceBreakDetail detail) {
        this.detail = detail;
    }


    /**
     * 报告详情对象
     *
     * @author chenyi
     * @date 2022/03/07
     */
    public static class PerformanceBreakDetail {

        /**
         * agentId
         */
        @SerializedName(ReportConstant.AGENT_ID)
        private Integer agentId;
        /**
         * 熔断时间
         */
        private Date breakDate;
        /**
         * 熔断前超限日志
         */
        @SerializedName(ReportConstant.LIMIT_PERFORMANCE_OVER_THRESHOLD_LOG)
        private List<PerformanceOverThresholdLog> performanceOverThresholdLog = new ArrayList<>();

        public Integer getAgentId() {
            return agentId;
        }

        public void setAgentId(Integer agentId) {
            this.agentId = agentId;
        }

        public Date getBreakDate() {
            return breakDate;
        }

        public void setBreakDate(Date breakDate) {
            this.breakDate = breakDate;
        }

        public List<PerformanceOverThresholdLog> getPerformanceOverThresholdLog() {
            return performanceOverThresholdLog;
        }

        public void setPerformanceOverThresholdLog(List<PerformanceOverThresholdLog> performanceOverThresholdLog) {
            this.performanceOverThresholdLog = performanceOverThresholdLog;
        }
    }


    /**
     * 性能超限日志
     *
     * @author chenyi
     * @date 2022/03/07
     */
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

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Integer getOverThresholdType() {
            return overThresholdType;
        }

        public void setOverThresholdType(Integer overThresholdType) {
            this.overThresholdType = overThresholdType;
        }

        public PerformanceMetrics getNowMetrics() {
            return nowMetrics;
        }

        public void setNowMetrics(PerformanceMetrics nowMetrics) {
            this.nowMetrics = nowMetrics;
        }

        public PerformanceMetrics getThreshold() {
            return threshold;
        }

        public void setThreshold(PerformanceMetrics threshold) {
            this.threshold = threshold;
        }

        public Integer getOverThresholdCount() {
            return overThresholdCount;
        }

        public void setOverThresholdCount(Integer overThresholdCount) {
            this.overThresholdCount = overThresholdCount;
        }


    }

}
