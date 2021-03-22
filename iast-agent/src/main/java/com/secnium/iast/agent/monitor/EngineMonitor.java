package com.secnium.iast.agent.monitor;

import com.secnium.iast.agent.IASTProperties;
import com.secnium.iast.agent.manager.EngineManager;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class EngineMonitor implements IMonitor {
    private final IASTProperties properties;
    private String currentStatus;
    private final EngineManager engineManager;

    public EngineMonitor(EngineManager engineManager) {
        this.engineManager = engineManager;
        this.properties = IASTProperties.getInstance();
        this.currentStatus = this.properties.getEngineStatus();
    }

    @Override
    public void check() {
        if (!currentStatus.equals(this.properties.getEngineStatus())) {
            if ("start".equals(this.properties.getEngineStatus())) {
                engineManager.start();
            } else if ("stop".equals(this.properties.getEngineStatus())) {
                engineManager.stop();
            } else if ("uninstall".equals(this.properties.getEngineStatus())) {
                engineManager.uninstall();
            } else if ("install".equals(this.properties.getEngineStatus())) {
                engineManager.install();
            }
            this.currentStatus = this.properties.getEngineStatus();
        }
    }
}
