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
public class VulnReport extends AbstractThread {
    private final Logger logger = LogUtils.getLogger(VulnReport.class);

    public VulnReport(long waitTime) {
        super(getThreadName(), true, waitTime);
    }

    private static String getThreadName() {
        return "Secnium Agent VulnReport";
    }

    @Override
    protected void send() throws Exception {
        while (EngineManager.hasNewReport()) {
            String report = EngineManager.getNewReport();
            try {
                if (report != null && !report.isEmpty()) {
                    boolean success = HttpClientUtils.sendPost(Constants.API_REPORT_UPLOAD, report);
                    if (!success) {
                        logger.error("report send failure.");
                    }
                }
            } catch (Exception e) {
                logger.info(report);
                throw e;
            }
        }
    }
}
