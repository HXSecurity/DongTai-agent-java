package io.dongtai.iast.core.service;

import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.iast.common.constants.ReportKey;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.scope.ScopeManager;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

/**
 * 上报agent队列与请求数量
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ServerAddressReport implements Runnable {

    private String serverAddr;
    private Integer serverPort;
    private String protocol;
    public ServerAddressReport(String serverAddr,Integer serverPort,String protocol) {
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.protocol = protocol;
    }

    public String getServereAddressMsg() {
        JSONObject report = new JSONObject();
        report.put(ReportKey.AGENT_ID, EngineManager.getAgentId());
        report.put("serverAddr", this.serverAddr);
        report.put("serverPort", this.serverPort);
        report.put("protocol", this.protocol);
        return report.toString();
    }

    @Override
    public void run() {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();
            ThreadPools.sendReport(ApiPath.AGENT_UPDATE, this.getServereAddressMsg());
        } catch (Exception e) {
            DongTaiLog.error("send API Queue to {} error, reason: {}", ApiPath.REPORT_UPLOAD, e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
    }
}
