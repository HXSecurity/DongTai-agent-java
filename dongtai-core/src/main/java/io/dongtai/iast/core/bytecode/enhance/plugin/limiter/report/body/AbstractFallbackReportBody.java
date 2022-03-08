package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.body;

/**
 * 降级报告内容
 *
 * @author liyuan40
 * @date 2022/3/8 17:24
 */
public abstract class AbstractFallbackReportBody {

    private String fallbackType;

    public AbstractFallbackReportBody(String fallbackType) {
        this.fallbackType = fallbackType;
    }

    public String getFallbackType() {
        return fallbackType;
    }

    public void setFallbackType(String fallbackType) {
        this.fallbackType = fallbackType;
    }
}
