package io.dongtai.iast.core.handler.hookpoint.api;

import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.iast.common.constants.ReportKey;
import io.dongtai.iast.common.constants.ReportType;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.service.ThreadPools;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

/**
 * @author CC11001100
 * @since v1.12.0
 */
public abstract class AbstractApiGatherThread extends Thread {

    public AbstractApiGatherThread(String name) {
        super(name);
    }

    protected void report(Object openApi, String framework) {
        if (openApi == null) {
            return;
        }
        try {
            String report = createReport(openApi, framework);
            ThreadPools.sendReport(ApiPath.REPORT_UPLOAD, report);
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.API_COLLECTOR_GET_API_THREAD_EXECUTE_FAILED, e);
        }
    }

    private String createReport(Object openApi, String framework) {
        JSONObject report = new JSONObject();
        report.put(ReportKey.TYPE, ReportType.API_V2);

        JSONObject detail = new JSONObject();
        detail.put(ReportKey.AGENT_ID, EngineManager.getAgentId());
        detail.put("framework", framework);
        detail.put(ReportKey.API_DATA, openApi);

        report.put(ReportKey.DETAIL, detail);

        return report.toString();
    }

}
