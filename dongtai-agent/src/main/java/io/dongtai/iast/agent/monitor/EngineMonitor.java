package io.dongtai.iast.agent.monitor;

import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.report.AgentRegisterReport;
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

        if ("notcmd".equals(status)){
            return;
        }

        if ("coreRegisterStart".equals(status)) {
            isCoreRegisterStart = true;
            startEngine();
        }else if ("coreStop".equals(status) && isCoreRegisterStart) {
            DongTaiLog.info("engine stop");
            engineManager.stop();
        } else if ("coreStart".equals(status) && isCoreRegisterStart) {
            DongTaiLog.info("engine start");
            engineManager.start();
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
            DongTaiLog.info("Engine Monitor check");
            this.check();
            MonitorDaemonThread.threadSleep();
        }
    }
}
