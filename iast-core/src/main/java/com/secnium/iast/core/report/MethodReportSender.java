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
public class MethodReportSender extends AbstractThread {

    private final Logger logger = LogUtils.getLogger(MethodReportSender.class);

    @Override
    protected void send() throws Exception {
        while (EngineManager.hasMethodReport()) {
            String report = EngineManager.getMethodReport();
            try {
                if (report != null && !report.isEmpty()) {
                    HttpClientUtils.sendPost(Constants.API_REPORT_UPLOAD, report);
                }
            } catch (Exception e) {
                logger.info(report);
                logger.error("send method report error, reason: ", e);
            }
        }
    }
}
