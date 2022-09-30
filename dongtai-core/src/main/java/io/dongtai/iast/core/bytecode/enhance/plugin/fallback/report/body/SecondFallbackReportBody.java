package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.body;

import com.google.gson.annotations.SerializedName;
import io.dongtai.iast.common.constants.ReportKey;
import io.dongtai.iast.common.constants.ReportType;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.FallbackSwitch;
import io.dongtai.iast.core.utils.StopwatchUtils;
import lombok.*;
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
    @SerializedName(ReportKey.TYPE)
    private Integer type = ReportType.SECOND_FALLBACK;

    /**
     * detail 字段
     */
    @SerializedName(ReportKey.DETAIL)
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
        @SerializedName(ReportKey.AGENT_ID)
        private Integer agentId;

        /**
         * 详细日志
         */
        @SerializedName("secondFallbackOverThresholdLog")
        private LinkedList<AbstractSecondFallbackReportLog> secondFallbackReportDetailLog;

        public SecondFallbackReportDetail(LinkedList<AbstractSecondFallbackReportLog> secondFallbackReportDetailLog) {
            this.secondFallbackReportDetailLog = secondFallbackReportDetailLog;
        }
    }

    @Data
    @NoArgsConstructor
    public static abstract class AbstractSecondFallbackReportLog {

        private String fallbackType;

        public AbstractSecondFallbackReportLog(FallbackSwitch.SecondFallbackReasonEnum fallbackType) {
            this.fallbackType = fallbackType.getFallbackType();
        }

    }

    /**
     * 熔断器熔断频率超限日志
     *
     * @author liyuan
     * @date 2022/03/10
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class FrequencyOverThresholdLog extends AbstractSecondFallbackReportLog {
        /**
         * 发生时间
         */
        private Date occurTime;

        public FrequencyOverThresholdLog(FallbackSwitch.SecondFallbackReasonEnum fallbackType) {
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
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class DurationOverThresholdLog extends AbstractSecondFallbackReportLog {

        /**
         * 开始时间
         */
        private Date startTime;

        /**
         * 持续时间(单位:ms)
         */
        private Long persistTime;

        /**
         * 阈值(单位:ms)
         */
        private Long threshold;

        public DurationOverThresholdLog(FallbackSwitch.SecondFallbackReasonEnum secondFallbackType, StopWatch stopWatch, Long threshold) {
            super(secondFallbackType);
            Long startTime = StopwatchUtils.getStartTime(stopWatch);
            this.startTime = startTime == null ? null : new Date(startTime);
            this.persistTime = StopwatchUtils.getTime(stopWatch);
            this.threshold = threshold;
        }
    }
}
