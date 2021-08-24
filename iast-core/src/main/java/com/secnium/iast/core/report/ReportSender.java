package com.secnium.iast.core.report;

import com.secnium.iast.core.AbstractThread;
import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.util.Constants;
import com.secnium.iast.core.util.HttpClientUtils;
import com.secnium.iast.core.util.LogUtils;
import org.slf4j.Logger;

/**
 * 发送报告的功能实现
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ReportSender extends AbstractThread {
    private final Logger logger = LogUtils.getLogger(ReportSender.class);
//    private final Pattern PATTERN = Pattern.compile("\"type\":1}");

    @Override
    protected void send() throws Exception {
        String report;
        while (EngineManager.hasNewReport()) {
            report = EngineManager.getNewReport();
            if (logger.isDebugEnabled()) {
                logger.debug(report);
            }
            if (report != null && !report.isEmpty()) {
                HttpClientUtils.sendPost(Constants.API_REPORT_UPLOAD, report);
            }
        }
    }
}
