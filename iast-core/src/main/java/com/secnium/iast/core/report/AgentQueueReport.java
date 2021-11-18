package com.secnium.iast.core.report;

import com.secnium.iast.core.AbstractThread;
import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.replay.HttpRequestReplay;
import com.secnium.iast.core.util.Constants;
import com.secnium.iast.core.util.HttpClientUtils;
import com.secnium.iast.core.util.LogUtils;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 * 上报agent队列与请求数量
 *
 * @author dongzhiyong@huoxian.cn
 */
public class AgentQueueReport extends AbstractThread {

    private final Logger logger = LogUtils.getLogger(getClass());

    public static String generateHeartBeatMsg() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_HEART_BEAT);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);
        detail.put(ReportConstant.AGENT_ID, EngineManager.getAgentId());
        detail.put(ReportConstant.REQ_COUNT, EngineManager.getRequestCount());
        detail.put(ReportConstant.REPORT_QUEUE, EngineManager.getReportQueueSize());
        detail.put(ReportConstant.METHOD_QUEUE, EngineManager.getMethodReportQueueSize());
        detail.put(ReportConstant.REPLAY_QUEUE, EngineManager.getReplayQueueSize());
        detail.put(ReportConstant.KEY_RETURN_QUEUE, 1);

        return report.toString();
    }

    /**
     * 发送请求
     *
     * @throws Exception 捕获发送报告过程中出现的异常
     */
    @Override
    protected void send() throws Exception {
        try {
            StringBuilder response = HttpClientUtils.sendPost(Constants.API_REPORT_UPLOAD, generateHeartBeatMsg());
            HttpRequestReplay.sendReplayRequest(response);
        } catch (Exception e) {
            logger.error("report error, reason: ", e);
        }
    }
}
