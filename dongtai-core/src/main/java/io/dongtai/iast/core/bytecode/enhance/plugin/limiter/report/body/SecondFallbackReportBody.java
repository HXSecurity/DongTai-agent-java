package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.body;

import com.google.gson.annotations.SerializedName;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.fallback.SecondFallbackSwitch;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import io.dongtai.iast.core.utils.RemoteConfigUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Date;
import java.util.LinkedList;

/**
 * 二次降级报告
 *
 * @author liyuan40
 * @date 2022/3/10 00:39
 */
@Data
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

    public SecondFallbackReportBody(LinkedList<AbstractSecondFallbackReportLog> secondFallbackReportDetailLog) {
        detail = new SecondFallbackReportDetail(secondFallbackReportDetailLog);
    }

    /**
     * 是否存在日志
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return detail.getSecondFallbackReportDetailLog().isEmpty();
    }

    /**
     * 添加日志
     *
     * @param log 日志
     */
    public void addSecondFallbackReportLog(AbstractSecondFallbackReportLog log) {
        detail.getSecondFallbackReportDetailLog().add(log);
    }

    /**
     * 清理日志
     */
    public void clear() {
        detail.getSecondFallbackReportDetailLog().clear();
    }

    /**
     * 二次降级报告内容
     *
     * @author liyuan
     * @date 2022/03/10
     */
    @Data
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
    }

    @Data
    @NoArgsConstructor
    public static abstract class AbstractSecondFallbackReportLog {

        private String fallbackType;

        public AbstractSecondFallbackReportLog(SecondFallbackSwitch.SecondFallbackTypeEnum fallbackType) {
            this.fallbackType = fallbackType.getFallbackType();
        }

    }

    /**
     * 熔断器熔断频率超限日志
     *
     * @author liyuan
     * @date 2022/03/10
     */
    @Data
    public static class SwitchFrequencyOverThresholdLog extends AbstractSecondFallbackReportLog {
        /**
         * 发生时间
         */
        private Date occurTime;

        public SwitchFrequencyOverThresholdLog(SecondFallbackSwitch.SecondFallbackTypeEnum fallbackType) {
            super(fallbackType);
            this.occurTime = new Date();
        }
    }

    /**
     * 熔断器打开状态持续时间超过阈值的二次降级报告内容
     *
     * @author liyuan40
     * @date 2022/3/8 17:10
     */
    @Data
    public static class SwitchOpenTimeOverThresholdReportLog extends AbstractSecondFallbackReportLog {

        /**
         * 开始时间
         */
        private Date startTime;

        /**
         * 持续时间
         */
        private String persistTime;

        /**
         * 阈值
         */
        private String threshold;

        public SwitchOpenTimeOverThresholdReportLog(SecondFallbackSwitch.SecondFallbackTypeEnum secondFallbackType, StopWatch stopWatch) {
            super(secondFallbackType);
            this.startTime = new Date(stopWatch.getStartTime());
            this.persistTime = stopWatch.getTime() / 1000 + "s";
            this.threshold = RemoteConfigUtils.getSwitchOpenStatusDurationThreshold(null) / 1000 + "s";
        }
    }
}
