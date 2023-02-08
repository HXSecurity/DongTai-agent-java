package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.report.HeartBeatReport;
import io.dongtai.iast.agent.util.HttpClientUtils;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.iast.common.state.State;
import io.dongtai.iast.common.state.StateCause;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class AgentStateMonitor implements IMonitor {
    private final EngineManager engineManager;
    public static Boolean isCoreRegisterStart = false;
    private static final String NAME = "AgentStateMonitor";

    public AgentStateMonitor(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    @Override
    public String getName() {
        return AgentConstant.THREAD_NAME_PREFIX + NAME;
    }

    @Override
    public void check() {
        try {
            if (this.engineManager.getAgentState().getState() == null) {
                return;
            }

            if (this.engineManager.getAgentState().isUninstalledByCli()) {
                return;
            }

            if (!this.engineManager.getAgentState().isFallback() && !this.engineManager.getAgentState().isException()) {
                String expectState = checkExpectState();
                if (State.RUNNING.equals(expectState) && this.engineManager.getAgentState().isPaused()) {
                    DongTaiLog.info("engine start by server expect state");
                    engineManager.start();
                    engineManager.getAgentState().setState(State.RUNNING).setCause(StateCause.RUNNING_BY_SERVER);
                } else if (State.PAUSED.equals(expectState) && this.engineManager.getAgentState().isRunning()) {
                    DongTaiLog.info("engine stop by server expect state");
                    engineManager.stop();
                    engineManager.getAgentState().setState(State.PAUSED).setCause(StateCause.PAUSE_BY_SERVER);
                }
            }
            HttpClientUtils.sendPost(ApiPath.ACTUAL_ACTION,
                    HeartBeatReport.generateAgentActualActionMsg(this.engineManager.getAgentState()));
        } catch (Throwable t) {
            DongTaiLog.warn(ErrorCode.AGENT_MONITOR_THREAD_CHECK_FAILED, getName(), t);
        }
    }

    private String checkExpectState() {
        try {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("agentId", String.valueOf(AgentRegisterReport.getAgentId()));
            String respRaw = HttpClientUtils.sendGet(ApiPath.EXCEPT_ACTION, parameters).toString();
            if (!respRaw.isEmpty()) {
                JSONObject resp = new JSONObject(respRaw);
                JSONObject data = (JSONObject) resp.get("data");
                return data.get("exceptRunningStatus").toString();
            }
        } catch (Throwable e) {
            return "other";
        }
        return "other";
    }

    @Override
    public void run() {
        try {
            while (!MonitorDaemonThread.isExit) {
                this.check();
                ThreadUtils.threadSleep(30);
            }
        } catch (Throwable t) {
            DongTaiLog.debug("{} interrupted: {}", getName(), t.getMessage());
        }
    }
}
