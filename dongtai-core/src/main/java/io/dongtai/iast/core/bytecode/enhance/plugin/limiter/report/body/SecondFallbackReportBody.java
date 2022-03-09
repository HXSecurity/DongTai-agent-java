package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.body;

import com.google.gson.annotations.SerializedName;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.fallback.LimitFallbackSwitch;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import io.dongtai.iast.core.utils.RemoteConfigUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.text.SimpleDateFormat;
import java.util.LinkedList;

/**
 * 二次降级报告
 *
 * @author liyuan40
 * @date 2022/3/10 00:39
 */
public class SecondFallbackReportBody {
    /**
     * type 字段
     */
    @SerializedName(ReportConstant.REPORT_KEY)
    private Integer reportKey = ReportConstant.REPORT_SECOND_FALLBACK;

    /**
     * detail 字段
     */
    @SerializedName(ReportConstant.REPORT_VALUE_KEY)
    private SecondFallbackReportDetail detail;

    public Integer getReportKey() {
        return reportKey;
    }

    public SecondFallbackReportDetail getDetail() {
        return detail;
    }

    public void setDetail(SecondFallbackReportDetail detail) {
        this.detail = detail;
    }

    public SecondFallbackReportBody(LinkedList<AbstractSecondFallbackReportLog> secondFallbackReportDetailLog) {
        detail = new SecondFallbackReportDetail(secondFallbackReportDetailLog);
    }

    /**
     * 二次降级报告内容
     *
     * @author liyuan
     * @date 2022/03/10
     */
    public static class SecondFallbackReportDetail {
        /**
         * IAST agent 编号
         */
        @SerializedName(ReportConstant.AGENT_ID)
        private Integer agentId;

        /**
         * 详细日志
         */
        @SerializedName(ReportConstant.SECOND_FALLBACK_OVER_THRESHOLD_LOG)
        private LinkedList<AbstractSecondFallbackReportLog> secondFallbackReportDetailLog;

        public SecondFallbackReportDetail(LinkedList<AbstractSecondFallbackReportLog> secondFallbackReportDetailLog) {
            this.agentId = EngineManager.getAgentId();
            this.secondFallbackReportDetailLog = secondFallbackReportDetailLog;
        }

        public Integer getAgentId() {
            return agentId;
        }

        public void setAgentId(Integer agentId) {
            this.agentId = agentId;
        }

        public void setSecondFallbackReportDetailLog(LinkedList<AbstractSecondFallbackReportLog> secondFallbackReportDetailLog) {
            this.secondFallbackReportDetailLog = secondFallbackReportDetailLog;
        }

        public LinkedList<AbstractSecondFallbackReportLog> getSecondFallbackReportDetailLog() {
            return secondFallbackReportDetailLog;
        }
    }

    public static abstract class AbstractSecondFallbackReportLog {

        private String fallbackType;

        public AbstractSecondFallbackReportLog(String fallbackType) {
            this.fallbackType = fallbackType;
        }

        public String getFallbackType() {
            return fallbackType;
        }

        public void setFallbackType(String fallbackType) {
            this.fallbackType = fallbackType;
        }
    }

    /**
     * 熔断器熔断频率超限日志
     *
     * @author liyuan
     * @date 2022/03/10
     */
    public static class SwitchFrequencyOverThresholdLog extends AbstractSecondFallbackReportLog {
        /**
         * 发生时间
         */
        private String occurTime;

        public SwitchFrequencyOverThresholdLog(String fallbackType, String occurTime) {
            super(fallbackType);
            this.occurTime = occurTime;
        }

        public String getOccurTime() {
            return occurTime;
        }

        public void setOccurTime(String occurTime) {
            this.occurTime = occurTime;
        }
    }

    /**
     * 熔断器打开状态持续时间超过阈值的二次降级报告内容
     *
     * @author liyuan40
     * @date 2022/3/8 17:10
     */
    public static class SwitchOpenTimeOverThresholdReportLog extends AbstractSecondFallbackReportLog {

        /**
         * 开始时间
         */
        private String startTime;

        /**
         * 持续时间
         */
        private String persistTime;

        /**
         * 阈值
         */
        private String threshold;

        public SwitchOpenTimeOverThresholdReportLog(LimitFallbackSwitch.SecondFallbackTypeEnum secondFallbackType, StopWatch stopWatch) {
            super(secondFallbackType.getFallbackType());
            this.startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(stopWatch.getStartTime());
            this.persistTime = stopWatch.getTime() / 1000 + "s";
            this.threshold = RemoteConfigUtils.getSwitchOpenStatusDurationThreshold(null) / 1000 + "s";
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getPersistTime() {
            return persistTime;
        }

        public void setPersistTime(String persistTime) {
            this.persistTime = persistTime;
        }

        public String getThreshold() {
            return threshold;
        }

        public void setThreshold(String threshold) {
            this.threshold = threshold;
        }
    }
}
