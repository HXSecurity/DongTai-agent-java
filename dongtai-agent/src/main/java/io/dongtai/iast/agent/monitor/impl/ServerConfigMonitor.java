package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.agent.util.http.HttpClientUtils;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

public class ServerConfigMonitor implements IMonitor {
    private final String name = "ServerConfigMonitor";

    public JSONObject serverConfig = new JSONObject();

    @Override
    public String getName() {
        return Constant.THREAD_PREFIX + name;
    }

    @Override
    public void check() {
        DongTaiLog.info(("Server Config Check"));
        getConfig();
    }

    @Override
    public void run() {
        while (!MonitorDaemonThread.isExit){
            DongTaiLog.info("Server Config Monitor Check");
            check();
            ThreadUtils.threadSleep(60);
        }
    }

    /**
     * 根据agentID获取服务端对Agent的配置
     */
    public void getConfig(){
        DongTaiLog.info("Get Server Config");
        JSONObject report = new JSONObject();
        StringBuilder response;
        report.put(Constant.KEY_AGENT_ID, AgentRegisterReport.getAgentFlag());
        try {
            response = HttpClientUtils.sendPost(Constant.API_Server_Config,report.toString());
            serverConfig= (JSONObject) JSONObject.stringToValue(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
