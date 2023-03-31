package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.HttpClientUtils;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.common.config.ConfigBuilder;
import io.dongtai.iast.common.config.ConfigKey;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.util.HashMap;
import java.util.Map;

public class ConfigMonitor implements IMonitor {
    private static final String NAME = "ConfigMonitor";

    @Override
    public String getName() {
        return AgentConstant.THREAD_NAME_PREFIX + NAME;
    }

    @Override
    public void check() {
        try {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("agent_id", String.valueOf(AgentRegisterReport.getAgentId()));

            StringBuilder response = HttpClientUtils.sendGet(ApiPath.AGENT_CONFIG, parameters);
            ConfigBuilder.getInstance().updateFromRemote(response.toString());

            updateConfig();
        } catch (Throwable t) {
            DongTaiLog.warn(ErrorCode.AGENT_MONITOR_THREAD_CHECK_FAILED, t);
        }
    }

    private void updateConfig() {
        Boolean enableLog = ConfigBuilder.getInstance().get(ConfigKey.ENABLE_LOGGER);
        if (enableLog != null) {
            DongTaiLog.ENABLED = enableLog;
        }

        String logLevel = ConfigBuilder.getInstance().get(ConfigKey.LOGGER_LEVEL);
        if (logLevel != null) {
            DongTaiLog.setLevel(DongTaiLog.parseLevel(logLevel));
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
