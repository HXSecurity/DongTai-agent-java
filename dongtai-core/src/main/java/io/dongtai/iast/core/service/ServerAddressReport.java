package io.dongtai.iast.core.service;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import io.dongtai.iast.core.utils.Constants;
import io.dongtai.iast.core.utils.HttpClientUtils;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

import java.io.IOException;

/**
 * 上报agent队列与请求数量
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ServerAddressReport implements Runnable {

    private String serverAddr;
    private Integer serverPort;
    public ServerAddressReport(String serverAddr,Integer serverPort) {
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
    }

    public String getServereAddressMsg() {
        JSONObject report = new JSONObject();
        report.put(ReportConstant.AGENT_ID, EngineManager.getAgentId());
        report.put(ReportConstant.SERVER_ADDR, this.serverAddr);
        report.put(ReportConstant.SERVER_PORT, this.serverPort);
        return report.toString();
    }

    @Override
    public void run() {
        try {
            ThreadPools.sendReport(Constants.SERVER_ADDRESS, this.getServereAddressMsg());
        } catch (Exception e) {
            DongTaiLog.error("send API Queue to {} error, reason: {}", Constants.API_REPORT_UPLOAD, e);
        }
    }
}
