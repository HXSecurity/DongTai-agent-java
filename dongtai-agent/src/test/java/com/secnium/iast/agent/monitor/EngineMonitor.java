package com.secnium.iast.agent.monitor;

import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.http.HttpClientUtils;
import io.dongtai.iast.agent.Constant;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class EngineMonitor implements IMonitor {
    private String currentStatus = null;
    private final String  name = "EngineMonitor";
    private final EngineManager engineManager;

    public EngineMonitor(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    @Override
    public String getName() {
        return Constant.THREAD_PREFIX + name;
    }


    @Override
    public void check() {

        String status = checkForStatus();

        if ("coreRegisterStart".equals(status)) {
            System.out.println("第一次启动引擎");
            return;
        } else if (status.equals(this.currentStatus)) {
            return;
        } else if ("coreStop".equals(status)) {
            DongTaiLog.info("engine stop");
            engineManager.stop();
            if (currentStatus == null) {
                return;
            }
        } else if ("coreStart".equals(status)) {
            DongTaiLog.info("engine start");
            engineManager.start();
        }
        this.currentStatus = status;
    }

    private String checkForStatus() {
        try {
            String respRaw = String.valueOf(HttpClientUtils.sendGet(Constant.API_ENGINE_ACTION, "agentToken", AgentRegisterReport.getAgentToken()));
            if (respRaw != null && !respRaw.isEmpty()) {
                JSONObject resp = new JSONObject(respRaw);
                return resp.get("action").toString();
            }
        } catch (Exception e) {
            return "other";
        }
        return "other";
    }

    @Override
    public void run() {
        this.check();
        try{
            Thread.sleep(60 * 1000L);
        } catch (Throwable t) {
            DongTaiLog.info("Start engine monitor failed, msg : {}, error : {}",t.getMessage(),t.getCause());
        }
    }

}
