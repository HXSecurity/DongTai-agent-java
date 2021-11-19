package com.secnium.iast.core.report;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import org.json.JSONObject;


/**
 * 定时发送资产信息
 *
 * @author dongzhiyong@huoxian.cn
 */
public class AssestReport {

    public static void sendReport(String packagePath, String packageName, String signature, String algorithm) {
        String report = createReport(packagePath, packageName, signature, algorithm);
        EngineManager.sendNewReport(report);
    }

    private static String createReport(String packagePath, String packageName, String signature, String algorithm) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_SCA);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_ID, EngineManager.getAgentId());
        detail.put(ReportConstant.SCA_PACKAGE_PATH, packagePath);
        detail.put(ReportConstant.SCA_PACKAGE_NAME, packageName);
        detail.put(ReportConstant.SCA_PACKAGE_SIGNATURE, signature);
        detail.put(ReportConstant.SCA_PACKAGE_ALGORITHM, algorithm);

        return report.toString();
    }

}
