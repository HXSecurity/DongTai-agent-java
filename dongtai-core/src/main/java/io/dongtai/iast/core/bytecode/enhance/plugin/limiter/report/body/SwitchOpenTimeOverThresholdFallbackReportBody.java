package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.body;

/**
 * 熔断器打开状态持续时间超过阈值的二次降级报告内容
 *
 * @author liyuan40
 * @date 2022/3/8 17:10
 */
public class SwitchOpenTimeOverThresholdFallbackReportBody extends AbstractFallbackReportBody{

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 持续时间
     */
    private long persistTime;

    /**
     * 阈值
     */
    private long threshold;

    public SwitchOpenTimeOverThresholdFallbackReportBody(String fallbackSwitcher, String startTime, long persistTime, long threshold) {
        super(fallbackSwitcher);
        this.startTime = startTime;
        this.persistTime = persistTime;
        this.threshold = threshold;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public long getPersistTime() {
        return persistTime;
    }

    public void setPersistTime(long persistTime) {
        this.persistTime = persistTime;
    }

    public long getThreshold() {
        return threshold;
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }
}
