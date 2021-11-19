package com.secnium.iast.agent.monitor;

import com.secnium.iast.agent.*;
import com.secnium.iast.agent.manager.EngineManager;
import com.secnium.iast.agent.report.AgentRegisterReport;
import com.secnium.iast.agent.util.LogUtils;
import com.secnium.iast.agent.util.http.HttpClientUtils;
import org.json.JSONObject;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class EngineMonitor implements IMonitor {
    private String currentStatus = null;
    private final EngineManager engineManager;

    public EngineMonitor(EngineManager engineManager) {
        this.engineManager = engineManager;
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
            LogUtils.info("engine stop");
            engineManager.stop();
            if (currentStatus == null){
                return;
            }
        } else if ("coreStart".equals(status)) {
            LogUtils.info("engine start");
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
}
