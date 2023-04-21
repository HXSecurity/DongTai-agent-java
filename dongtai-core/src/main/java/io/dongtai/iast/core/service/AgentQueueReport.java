package io.dongtai.iast.core.service;

import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.common.constants.*;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.utils.HttpClientUtils;
import io.dongtai.log.DongTaiLog;

/**
 * 上报agent队列与请求数量
 *
 * @author dongzhiyong@huoxian.cn
 */
public class AgentQueueReport implements Runnable {

    public static String generateHeartBeatMsg() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportKey.TYPE, ReportType.HEART_BEAT);
        report.put(ReportKey.DETAIL, detail);
        detail.put(ReportKey.AGENT_ID, EngineManager.getAgentId());
        detail.put("reqCount", EngineManager.getRequestCount());
        detail.put("reportQueue", 0);
        detail.put("methodQueue", 0);
        detail.put("replayQueue", 0);
        detail.put(ReportKey.IS_CORE_INSTALLED, 1);
        detail.put(ReportKey.IS_CORE_RUNNING, EngineManager.isEngineRunning() ? 1 : 0);
        detail.put(ReportKey.RETURN_QUEUE, 1);

        return report.toString();
    }

    @Override
    public void run() {
        if (EngineManager.isEngineRunning()){
            try {
                StringBuilder replayRequestRaw = HttpClientUtils.sendPost(ApiPath.REPORT_UPLOAD, generateHeartBeatMsg());
                if (EngineManager.isEngineRunning()) {
                    ThreadPools.submitReplayTask(replayRequestRaw);
                }
            } catch (Throwable e) {
                DongTaiLog.debug("send API Queue to {} error, reason: {}", ApiPath.REPORT_UPLOAD, e.getMessage());
            }
        }
    }
}
