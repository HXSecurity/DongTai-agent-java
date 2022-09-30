package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report;

import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.iast.core.service.ThreadPools;
import io.dongtai.log.DongTaiLog;


/**
 * 限制报告上报器
 *
 * @author chenyi
 * @date 2022/2/28
 */
public abstract class AbstractLimitReport {

    protected static void sendReport(String report) {
        try {
            ThreadPools.sendLimitReport(ApiPath.REPORT_UPLOAD, report);
        } catch (Exception e) {
            DongTaiLog.error(e);
        }
    }

}
