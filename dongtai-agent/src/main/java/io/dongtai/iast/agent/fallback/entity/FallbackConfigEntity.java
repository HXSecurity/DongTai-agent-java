package io.dongtai.iast.agent.fallback.entity;

import java.util.List;
public class FallbackConfigEntity {

    /**
     * 是否允许自动降级
     */
    private Boolean enableAutoFallback;

    /**
     * 系统熔断后是否卸载
     */
    private Boolean systemIsUninstall;

    /**
     * 系统熔断阈值
     */
    private List<PerformanceEntity> system;

    public Boolean getEnableAutoFallback() {
        return enableAutoFallback;
    }

    public void setEnableAutoFallback(Boolean enableAutoFallback) {
        this.enableAutoFallback = enableAutoFallback;
    }

    public Boolean getSystemIsUninstall() {
        return systemIsUninstall;
    }

    public void setSystemIsUninstall(Boolean systemIsUninstall) {
        this.systemIsUninstall = systemIsUninstall;
    }

    public List<PerformanceEntity> getSystem() {
        return system;
    }

    public void setSystem(List<PerformanceEntity> system) {
        this.system = system;
    }
}
