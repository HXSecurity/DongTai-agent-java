package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.agent.util.http.HttpClientUtils;
import io.dongtai.iast.common.utils.serialize.SerializeUtils;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

public class ServerConfigMonitor implements IMonitor {
    private final String name = "ServerConfigMonitor";

    @Override
    public String getName() {
        return Constant.THREAD_PREFIX + name;
    }

    @Override
    public void check() {
        String serverConfig = getConfigFromRemote();
        setConfigToLocal(serverConfig);
    }

    @Override
    public void run() {
        while (!MonitorDaemonThread.isExit){
            check();
            ThreadUtils.threadSleep(60);
        }
    }

    /**
     * 根据agentID获取服务端对Agent的配置
     */
    public String getConfigFromRemote(){
        JSONObject report = new JSONObject();
        StringBuilder response = new StringBuilder();
        report.put(Constant.KEY_AGENT_ID, AgentRegisterReport.getAgentFlag());
        try {
            response = HttpClientUtils.sendPost(Constant.API_Server_Config,report.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    /**
     * 寻找远端配置工具类 反射调用进行阈值配置
     */
    public void setConfigToLocal(String serverConfig){
        try {
            final Class<?> remoteConfigUtil = EngineManager.getRemoteConfigUtils();
            remoteConfigUtil.getMethod("syncRemoteConfig", String.class)
                    .invoke(null, serverConfig);
        } catch (Throwable t) {
            DongTaiLog.error("syncRemoteConfig failed, msg:{}, err:{}", t.getMessage(), t.getCause());
        }

    }

}
