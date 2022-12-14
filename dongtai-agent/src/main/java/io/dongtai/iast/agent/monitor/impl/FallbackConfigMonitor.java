package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.fallback.FallbackConfig;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.log.DongTaiLog;

public class FallbackConfigMonitor implements IMonitor {
    private static final String NAME = "FallbackConfigMonitor";

    @Override
    public String getName() {
        return AgentConstant.THREAD_NAME_PREFIX + NAME;
    }

    @Override
    public void check() {
        try {
            FallbackConfig.syncRemoteConfigV2(AgentRegisterReport.getAgentId());
        } catch (Throwable t) {
            DongTaiLog.warn("sync remote fallback config failed, msg:{}, err:{}", t.getMessage(), t.getCause());
        }
    }

    @Override
    public void run() {
        try {
            while (!MonitorDaemonThread.isExit) {
                this.check();
                ThreadUtils.threadSleep(60);
            }
        } catch (Throwable t) {
            DongTaiLog.debug("FallbackConfigMonitor interrupted, msg:{}", t.getMessage());
        }
    }
}
