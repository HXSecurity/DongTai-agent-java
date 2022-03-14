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
    private static final String NAME = "ServerConfigMonitor";

    @Override
    public String getName() {
        return Constant.THREAD_PREFIX + NAME;
    }

    @Override
    public void check() throws Exception {
        try {
            setConfigToLocal(AgentRegisterReport.getAgentFlag());
        }catch(Throwable t){
            DongTaiLog.warn("Set server config to local failed, msg: {}, error: {}",t.getMessage(),t.getCause());
        }
    }

    @Override
    public void run() {
        while (!MonitorDaemonThread.isExit){
            try {
                // EngineManger初始化时会请求配置一次，所以ServerConfigMonitor首次不用运行
                ThreadUtils.threadSleep(60);
                this.check();
            } catch (Throwable t) {
                DongTaiLog.warn("Monitor thread checked error, monitor:{}, msg:{}, err:{}", getName(), t.getMessage(), t.getCause());
            }
        }
    }

    /**
     * 寻找远端配置工具类 反射调用进行阈值配置
     */
    public void setConfigToLocal(int agentId) {
        try {
            final Class<?> remoteConfigUtil = EngineManager.getRemoteConfigUtils();
            if (remoteConfigUtil == null) {
                return;
            }
            remoteConfigUtil.getMethod("syncRemoteConfig",int.class)
                    .invoke(null, agentId);
        } catch (Throwable t) {
            DongTaiLog.error("setConfigToLocal failed, msg:{}, err:{}", t.getMessage(), t.getCause());
        }
    }

}
