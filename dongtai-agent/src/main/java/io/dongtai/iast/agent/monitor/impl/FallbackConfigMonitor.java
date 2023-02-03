package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.fallback.FallbackConfig;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

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
            DongTaiLog.warn(ErrorCode.AGENT_MONITOR_THREAD_CHECK_FAILED, getName(), t);
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
            DongTaiLog.debug("{} interrupted: {}", getName(), t.getMessage());
        }
    }
}
