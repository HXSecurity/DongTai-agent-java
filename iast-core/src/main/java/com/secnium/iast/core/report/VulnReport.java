package com.secnium.iast.core.report;

import com.secnium.iast.core.AbstractThread;
import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.util.Constants;
import com.secnium.iast.core.util.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 发送报告的功能实现
 *
 * @author dongzhiyong@huoxian.cn
 */
public class VulnReport extends AbstractThread {
    private long waitTime;
    private final Logger logger = LoggerFactory.getLogger(VulnReport.class);

    public VulnReport(long waitTime) {
        super(getThreadName(), true, waitTime);
        this.waitTime = waitTime;
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
                        EngineManager.sendNewReport(report);
                    }
                }
            } catch (Exception e) {
                logger.info(report);
                throw e;
            } finally {
                if (waitTime != PropertyUtils.getInstance().getReportInterval()) {
                    waitTime = PropertyUtils.getInstance().getReportInterval();
                    setMilliseconds();
                }
            }
        }
    }

    public static void main(String[] args) {

    }
}
