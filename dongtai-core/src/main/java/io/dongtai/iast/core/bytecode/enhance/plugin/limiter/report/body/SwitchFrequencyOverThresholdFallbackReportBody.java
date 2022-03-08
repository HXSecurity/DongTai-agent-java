package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.body;

/**
 * 熔断器熔断批量超过阈值的二次降级报告内容
 *
 * @author liyuan40
 * @date 2022/3/8 17:26
 */
public class SwitchFrequencyOverThresholdFallbackReportBody extends AbstractFallbackReportBody {
    /**
     * 发生时间
     */
    private String occurTime;

    public SwitchFrequencyOverThresholdFallbackReportBody(String fallbackType, String occurTime) {
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
