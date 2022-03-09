package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.monitor.ServerCommandEnum;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.agent.util.http.HttpClientUtils;
import io.dongtai.iast.agent.Constant;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class EngineMonitor implements IMonitor {
    private final EngineManager engineManager;
    public static Boolean isCoreRegisterStart = false;
    private final String name = "EngineMonitor";


    public EngineMonitor(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    @Override
    public String getName() {
        return  Constant.THREAD_PREFIX + name;
    }


    @Override
    public void check() {
        String status = checkForStatus();
        ServerCommandEnum serviceCmdEnum = ServerCommandEnum.getEnum(status);
        if (serviceCmdEnum == null || serviceCmdEnum == ServerCommandEnum.NO_CMD) {
            return;
        }
        DongTaiLog.info("receive system command. cmd:{}, desc:{}", serviceCmdEnum.getCommand(), serviceCmdEnum.getDesc());
        switch (serviceCmdEnum) {
            case CORE_REGISTER_START:
                isCoreRegisterStart = true;
                startEngine();
                break;
            case CORE_START:
                DongTaiLog.info("engine start");
                engineManager.start();
                break;
            case CORE_STOP:
                DongTaiLog.info("engine stop");
                engineManager.stop();
                break;
            case CORE_UNINSTALL:
                DongTaiLog.info("engine uninstall");
                engineManager.uninstall();
            default:
        }
    }

    private String checkForStatus() {
        try {
            String respRaw = String.valueOf(HttpClientUtils.sendGet(Constant.API_ENGINE_ACTION, "agentId", String.valueOf(AgentRegisterReport.getAgentFlag())));
            if (respRaw != null && !respRaw.isEmpty()) {
                JSONObject resp = new JSONObject(respRaw);
                return resp.get("data").toString();
            }
        } catch (Exception e) {
            return "other";
        }
        return "other";
    }

    public void startEngine() {
        boolean status = engineManager.extractPackage();
        status = status && engineManager.install();
        status = status && engineManager.start();
        if (!status) {
            DongTaiLog.info("DongTai IAST started failure");
        }
    }

    @Override
    public void run() {
        while (!MonitorDaemonThread.isExit) {
            this.check();
            ThreadUtils.threadSleep(60);
        }
    }
}
