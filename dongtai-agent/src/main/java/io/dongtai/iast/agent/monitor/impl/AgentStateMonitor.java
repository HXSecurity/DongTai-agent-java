package io.dongtai.iast.agent.monitor.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.report.HeartBeatReport;
import io.dongtai.iast.agent.util.HttpClientUtils;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.iast.common.state.AgentState;
import io.dongtai.iast.common.state.State;
import io.dongtai.iast.common.state.StateCause;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class AgentStateMonitor implements IMonitor {
    private final EngineManager engineManager;
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
        AgentState agentState = this.engineManager.getAgentState();
        try {
            if (agentState.getState() == null) {
                return;
            }

            if (agentState.isUninstalledByCli()) {
                HttpClientUtils.sendPost(ApiPath.ACTUAL_ACTION,
                        HeartBeatReport.generateAgentActualActionMsg(agentState));
                return;
            }

            Map<String, Object> stringStringMap = checkExpectState();
            // 默认值
            String expectState = "other";
            boolean allowReport = true;

            if (stringStringMap != null) {
                expectState = stringStringMap.get("exceptRunningStatus").toString();
                if (null != stringStringMap.get("allowReport")) {
                    allowReport = !"0".equals(stringStringMap.get("allowReport").toString());
                }
            }

            if (allowReport && !agentState.isAllowReport()) {
                DongTaiLog.info("engine is allowed to report data");
                agentState.setAllowReport(allowReport);
            } else if (!allowReport && agentState.isAllowReport()) {
                DongTaiLog.info("engine is not allowed to report data");
                agentState.setAllowReport(allowReport);
            }

            if (!agentState.isFallback() && !agentState.isException() && agentState.isAllowReport() && agentState.isAllowReport()) {
                if (State.RUNNING.equals(expectState) && agentState.isPaused()) {
                    DongTaiLog.info("engine start by server expect state");
                    engineManager.start();
                    agentState.setState(State.RUNNING).setCause(StateCause.RUNNING_BY_SERVER);
                } else if (State.PAUSED.equals(expectState) && agentState.isRunning()) {
                    DongTaiLog.info("engine stop by server expect state");
                    engineManager.stop();
                    agentState.setState(State.PAUSED).setCause(StateCause.PAUSE_BY_SERVER);
                }
            }
            HttpClientUtils.sendPost(ApiPath.ACTUAL_ACTION,
                    HeartBeatReport.generateAgentActualActionMsg(agentState));
        } catch (Throwable t) {
            DongTaiLog.warn(ErrorCode.AGENT_MONITOR_THREAD_CHECK_FAILED, getName(), t);
        }
    }

    private Map<String, Object> checkExpectState() {
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("agentId", String.valueOf(AgentRegisterReport.getAgentId()));
            String respRaw = HttpClientUtils.sendGet(ApiPath.EXCEPT_ACTION, parameters).toString();
            if (!respRaw.isEmpty()) {
                JSONObject resp = JSON.parseObject(respRaw);
                JSONObject data = (JSONObject) resp.get("data");
                Map<String, Object> objectObjectHashMap = new HashMap<>(2);
                String s = data.toJSONString();
                objectObjectHashMap = JSON.parseObject(s, Map.class);
                return objectObjectHashMap;
            }
        } catch (Throwable e) {
            return null;
        }
        return null;
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
