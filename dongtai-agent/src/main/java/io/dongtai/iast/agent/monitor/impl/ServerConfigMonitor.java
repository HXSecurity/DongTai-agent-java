package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.agent.util.http.HttpClientUtils;
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
        // 前期无法从服务端获取config，返回的serverConfig为""，不进行下一步配置客户端。
        if(!"".equals(serverConfig)){
            //  获取的JSON字段不合法，抛异常，不进行下一步配置客户端
            try {
                JSONObject tempJson = new JSONObject(serverConfig);
                setConfigToLocal(tempJson.toString());
            }catch(Throwable t){
                DongTaiLog.warn("Set server config to local failed, msg: {}, error: {}",t.getMessage(),t.getCause());
            }
        }
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

        } catch (Throwable t) {
            // todo 现在无法获取服务端配置，不需要打印日志。等服务端上线后取消注释下面的代码
            // DongTaiLog.warn("Get server config failed, msg:{}, err:{}",t.getMessage(),t.getCause());
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
        //    引擎卸载后，无法调用core包里的方法。
        } catch (Throwable ignored) {
        }

    }

}
