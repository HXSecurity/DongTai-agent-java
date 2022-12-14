package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.HttpClientUtils;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.common.config.ConfigBuilder;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.log.DongTaiLog;

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
        } catch (Throwable t) {
            DongTaiLog.warn("request remote agent config failed", t);
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
            DongTaiLog.debug("ConfigMonitor interrupted, msg:{}", t.getMessage());
        }
    }
}
