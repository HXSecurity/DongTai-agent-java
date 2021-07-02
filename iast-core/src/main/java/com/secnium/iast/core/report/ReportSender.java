package com.secnium.iast.core.report;

import com.secnium.iast.core.AbstractThread;
import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.replay.HttpRequestReplay;
import com.secnium.iast.core.util.Constants;
import com.secnium.iast.core.util.HttpClientUtils;
import com.secnium.iast.core.util.LogUtils;
import org.slf4j.Logger;

import java.util.regex.Pattern;

/**
 * 发送报告的功能实现
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ReportSender extends AbstractThread {
    private final Logger logger = LogUtils.getLogger(ReportSender.class);
    private final Pattern PATTERN = Pattern.compile("\"type\":1}");

    @Override
    protected void send() throws Exception {
        HeartBeatReport.createReport();
        String report;
        StringBuilder response;
        while (EngineManager.hasNewReport()) {
            report = EngineManager.getNewReport();
            try {
                if (report != null && !report.isEmpty()) {
                    response = HttpClientUtils.sendPost(Constants.API_REPORT_UPLOAD, report);
                    if (PATTERN.matcher(report).find()) {
                        HttpRequestReplay.sendReplayRequest(response);
                    }
                }
            } catch (Exception e) {
                logger.info(report);
                throw e;
            }
        }
    }
}
