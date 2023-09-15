package io.dongtai.iast.core.service;

import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.iast.common.constants.ReportKey;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.log.DongTaiLog;

/**
 * 上报agent队列与请求数量
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ServerAddressReport implements Runnable {

    private final String serverAddr;
    private final Integer serverPort;
    private final String protocol;
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
            ThreadPools.sendReport(ApiPath.AGENT_UPDATE, this.getServereAddressMsg());
        } catch (Throwable e) {
            DongTaiLog.debug("send server address to {} error, reason: {}", ApiPath.REPORT_UPLOAD, e.getMessage());
        }
    }
}
