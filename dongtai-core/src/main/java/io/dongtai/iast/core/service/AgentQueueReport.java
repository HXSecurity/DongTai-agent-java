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
public class AgentQueueReport implements Runnable {

    public static String generateHeartBeatMsg() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_HEART_BEAT);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);
        detail.put(ReportConstant.AGENT_ID, EngineManager.getAgentId());
        detail.put(ReportConstant.REQ_COUNT, EngineManager.getRequestCount());
        detail.put(ReportConstant.REPORT_QUEUE, 0);
        detail.put(ReportConstant.METHOD_QUEUE, 0);
        detail.put(ReportConstant.REPLAY_QUEUE, 0);
        detail.put(ReportConstant.KEY_RETURN_QUEUE, 1);

        return report.toString();
    }

    @Override
    public void run() {
        try {
            StringBuilder replayRequestRaw = HttpClientUtils.sendPost(Constants.API_REPORT_UPLOAD, generateHeartBeatMsg());
            ThreadPools.submitReplayTask(replayRequestRaw);
        } catch (IOException e) {
            DongTaiLog.debug("send agent status failure, reason: {}", e);
        } catch (Exception e) {
            DongTaiLog.debug("send API Queue to {} error, reason: {}", Constants.API_REPORT_UPLOAD, e);
        }
    }
}
