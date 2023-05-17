package io.dongtai.iast.core.bytecode.sca;

import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.common.constants.*;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.utils.HttpClientUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

/**
 * 定时发送资产信息
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ScaReport {
    public final static String KEY_SCA_PACKAGES = "packages";
    public final static String KEY_SCA_PACKAGE_PATH = "packagePath";
    public final static String KEY_SCA_PACKAGE_NAME = "packageName";
    public final static String KEY_SCA_PACKAGE_SIGNATURE = "packageSignature";
    public final static String KEY_SCA_PACKAGE_ALGORITHM = "packageAlgorithm";

    public static void sendReport(String packagePath, String packageName, String signature, String algorithm) {
        String report = createReport(packagePath, packageName, signature, algorithm);
        sendReport(report);
    }

    public static void sendReport(String report) {
        try {
            HttpClientUtils.sendPost(ApiPath.REPORT_UPLOAD, report);
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("SCA_REPORT_SEND_FAILED"),
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
    }

    private static String createReport(String packagePath, String packageName, String signature, String algorithm) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportKey.TYPE, ReportType.SCA);
        report.put(ReportKey.DETAIL, detail);

        detail.put(ReportKey.AGENT_ID, EngineManager.getAgentId());
        detail.put(KEY_SCA_PACKAGE_PATH, packagePath);
        detail.put(KEY_SCA_PACKAGE_NAME, packageName);
        detail.put(KEY_SCA_PACKAGE_SIGNATURE, signature);
        detail.put(KEY_SCA_PACKAGE_ALGORITHM, algorithm);

        return report.toString();
    }

}
