package io.dongtai.iast.core.bytecode.sca;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import io.dongtai.iast.core.utils.Constants;
import io.dongtai.iast.core.utils.HttpClientUtils;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;


/**
 * 定时发送资产信息
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ScaReport {

    public static void sendReport(String packagePath, String packageName, String signature, String algorithm) {
        String report = createReport(packagePath, packageName, signature, algorithm);
        sendReport(report);
    }

    public static void sendReport(String report) {
        try {
            HttpClientUtils.sendPost(Constants.API_REPORT_UPLOAD, report);
        } catch (Exception e) {
            DongTaiLog.error(e);
        }
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
