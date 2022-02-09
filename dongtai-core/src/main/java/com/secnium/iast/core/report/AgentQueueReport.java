package com.secnium.iast.core.report;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.util.Constants;
import com.secnium.iast.core.util.HttpClientUtils;
import com.secnium.iast.log.DongTaiLog;
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
        boolean isRunning = EngineManager.isLingzhiRunning();
        if (isRunning) {
            EngineManager.turnOffLingzhi();
        }
        try {
            StringBuilder replayRequestRaw = HttpClientUtils.sendPost(Constants.API_REPORT_UPLOAD, generateHeartBeatMsg());
            ThreadPools.submitReplayTask(replayRequestRaw);
        } catch (IOException e) {
            DongTaiLog.error("report error reason: {}", e);
        } catch (Exception e) {
            DongTaiLog.error("report error, reason: {}", e);
        }
        if (isRunning) {
            EngineManager.turnOnLingzhi();
        }
    }
}
