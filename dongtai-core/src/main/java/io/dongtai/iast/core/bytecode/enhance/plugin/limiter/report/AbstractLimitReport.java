package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report;

import io.dongtai.iast.core.service.ErrorLogReport;
import io.dongtai.iast.core.service.ThreadPools;
import io.dongtai.iast.core.utils.Constants;


/**
 * 限制报告上报器
 *
 * @author chenyi
 * @date 2022/2/28
 */
public abstract class AbstractLimitReport {

    protected static void sendReport(String report) {
        try {
            ThreadPools.sendLimitReport(Constants.API_REPORT_UPLOAD, report);
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

}
