package com.secnium.iast.core.report;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.util.Constants;
import com.secnium.iast.core.util.HttpClientUtils;
import org.json.JSONObject;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class AgentRegisterReport {
    private static String AGENT_NAME = null;

    public static String getAgentToken() {
        if (AGENT_NAME == null) {
            PropertyUtils cfg = PropertyUtils.getInstance();
            String osName = System.getProperty("os.name");
            String hostname = HeartBeatReport.getInternalHostName();
            AGENT_NAME = osName + "-" + hostname + "-" + ReportConstant.AGENT_VERSION_VALUE + "-" + cfg.getEngineName();
        }
        return AGENT_NAME;
    }

    public static void send() {
        try {
            String msg = generateAgentRegisterMsg();
            HttpClientUtils.sendPost(Constants.API_AGENT_REGISTER, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String generateAgentRegisterMsg() {
        JSONObject object = new JSONObject();
        object.put("name", getAgentToken());
        object.put("version", ReportConstant.AGENT_VERSION_VALUE);
        return object.toString();
    }

    public static void main(String[] args) {
        PropertyUtils.getInstance("～/.iast/config/iast.properties");
        AgentRegisterReport.send();
    }
}
