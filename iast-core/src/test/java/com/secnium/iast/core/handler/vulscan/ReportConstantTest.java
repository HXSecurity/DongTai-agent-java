package com.secnium.iast.core.handler.vulscan;

import org.json.JSONObject;
import org.junit.Test;

public class ReportConstantTest {
    @Test
    public void generateHeartBeatReport() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_HEART_BEAT);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.HEART_BEAT_PID, 20102);
        detail.put(ReportConstant.HEART_BEAT_NETWORK, 20102);
        detail.put(ReportConstant.HEART_BEAT_MEMORY, 20102);
        detail.put(ReportConstant.HEART_BEAT_CPU, 20102);
        detail.put(ReportConstant.HEART_BEAT_DISK, 20102);
        detail.put(ReportConstant.HEART_BEAT_REQ_COUNT, 20102);
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_NAME, 20102);
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_VERSION, 20102);
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_PATH, 20102);
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_HOSTNAME, 20102);
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_IP, 20102);
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_PORT, 20102);

        System.out.println("心跳数据报告：");
        System.out.println(report.toString());
    }

    /**
     * todo: 如何提取第三方包内部的依赖项？如何将第三方包与应用关联起来
     */
    @Test
    public void generateScaReport() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_SCA);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.SCA_PACKAGE_PATH, 20102);
        detail.put(ReportConstant.SCA_PACKAGE_NAME, 20102);
        detail.put(ReportConstant.SCA_PACKAGE_SIGNATURE, 20102);
        detail.put(ReportConstant.SCA_PACKAGE_ALGORITHM, 20102);

        System.out.println("SCA数据报告：");
        System.out.println(report.toString());
    }

    @Test
    public void generateVulnNormalReport() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_NORNAL);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.COMMON_APP_NAME, 20102);
        detail.put(ReportConstant.COMMON_SERVER_NAME, 20102);
        detail.put(ReportConstant.COMMON_SERVER_PORT, 20102);
        detail.put(ReportConstant.COMMON_REMOTE_IP, 20102);
        detail.put(ReportConstant.COMMON_HTTP_PROTOCOL, 20102);
        detail.put(ReportConstant.COMMON_HTTP_SCHEME, 20102);
        detail.put(ReportConstant.COMMON_HTTP_METHOD, 20102);
        detail.put(ReportConstant.COMMON_HTTP_URL, 20102);
        detail.put(ReportConstant.COMMON_HTTP_QUERY_STRING, 20102);
        detail.put(ReportConstant.COMMON_HTTP_BODY, 20102);
        detail.put(ReportConstant.VULN_CALLER, 20102);

        System.out.println("普通漏洞数据报告：");
        System.out.println(report.toString());
    }

    @Test
    public void generateVulnDynamicReport() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_NORNAL);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.COMMON_APP_NAME, 20102);
        detail.put(ReportConstant.COMMON_SERVER_NAME, 20102);
        detail.put(ReportConstant.COMMON_SERVER_PORT, 20102);
        detail.put(ReportConstant.COMMON_REMOTE_IP, 20102);
        detail.put(ReportConstant.COMMON_HTTP_PROTOCOL, 20102);
        detail.put(ReportConstant.COMMON_HTTP_SCHEME, 20102);
        detail.put(ReportConstant.COMMON_HTTP_METHOD, 20102);
        detail.put(ReportConstant.COMMON_HTTP_URL, 20102);
        detail.put(ReportConstant.COMMON_HTTP_QUERY_STRING, 20102);
        detail.put(ReportConstant.COMMON_HTTP_BODY, 20102);
        detail.put(ReportConstant.VULN_CALLER, 20102);

        System.out.println("污点跟踪漏洞数据报告：");
        System.out.println(report.toString());
    }

    @Test
    public void generateVulnOverPowerReport() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_NORNAL);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.COMMON_APP_NAME, 20102);
        detail.put(ReportConstant.COMMON_SERVER_NAME, 20102);
        detail.put(ReportConstant.COMMON_SERVER_PORT, 20102);
        detail.put(ReportConstant.COMMON_REMOTE_IP, 20102);
        detail.put(ReportConstant.COMMON_HTTP_PROTOCOL, 20102);
        detail.put(ReportConstant.COMMON_HTTP_SCHEME, 20102);
        detail.put(ReportConstant.COMMON_HTTP_METHOD, 20102);
        detail.put(ReportConstant.COMMON_HTTP_URL, 20102);
        detail.put(ReportConstant.COMMON_HTTP_QUERY_STRING, 20102);
        detail.put(ReportConstant.COMMON_HTTP_BODY, 20102);
        detail.put(ReportConstant.VULN_CALLER, 20102);

        System.out.println("越权漏洞数据报告：");
        System.out.println(report.toString());
    }

    @Test
    public void generateAuthAddReport() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_NORNAL);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.COMMON_APP_NAME, 20102);
        detail.put(ReportConstant.COMMON_SERVER_NAME, 20102);
        detail.put(ReportConstant.COMMON_SERVER_PORT, 20102);
        detail.put(ReportConstant.COMMON_REMOTE_IP, 20102);
        detail.put(ReportConstant.COMMON_HTTP_PROTOCOL, 20102);
        detail.put(ReportConstant.COMMON_HTTP_SCHEME, 20102);
        detail.put(ReportConstant.COMMON_HTTP_METHOD, 20102);
        detail.put(ReportConstant.COMMON_HTTP_URL, 20102);
        detail.put(ReportConstant.COMMON_HTTP_QUERY_STRING, 20102);
        detail.put(ReportConstant.COMMON_HTTP_BODY, 20102);
        detail.put(ReportConstant.VULN_CALLER, 20102);

        System.out.println("新增权限数据报告：");
        System.out.println(report.toString());
    }

    @Test
    public void generateAuthUpdateReport() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_NORNAL);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.COMMON_APP_NAME, 20102);
        detail.put(ReportConstant.COMMON_SERVER_NAME, 20102);
        detail.put(ReportConstant.COMMON_SERVER_PORT, 20102);
        detail.put(ReportConstant.COMMON_REMOTE_IP, 20102);
        detail.put(ReportConstant.COMMON_HTTP_PROTOCOL, 20102);
        detail.put(ReportConstant.COMMON_HTTP_SCHEME, 20102);
        detail.put(ReportConstant.COMMON_HTTP_METHOD, 20102);
        detail.put(ReportConstant.COMMON_HTTP_URL, 20102);
        detail.put(ReportConstant.COMMON_HTTP_QUERY_STRING, 20102);
        detail.put(ReportConstant.COMMON_HTTP_BODY, 20102);
        detail.put(ReportConstant.VULN_CALLER, 20102);

        System.out.println("权限更新数据报告：");
        System.out.println(report.toString());
    }

    @Test
    public void generateErrorLogReport() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_NORNAL);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.COMMON_APP_NAME, 20102);
        detail.put(ReportConstant.COMMON_SERVER_NAME, 20102);
        detail.put(ReportConstant.COMMON_SERVER_PORT, 20102);
        detail.put(ReportConstant.COMMON_REMOTE_IP, 20102);
        detail.put(ReportConstant.COMMON_HTTP_PROTOCOL, 20102);
        detail.put(ReportConstant.COMMON_HTTP_SCHEME, 20102);
        detail.put(ReportConstant.COMMON_HTTP_METHOD, 20102);
        detail.put(ReportConstant.COMMON_HTTP_URL, 20102);
        detail.put(ReportConstant.COMMON_HTTP_QUERY_STRING, 20102);
        detail.put(ReportConstant.COMMON_HTTP_BODY, 20102);
        detail.put(ReportConstant.VULN_CALLER, 20102);

        System.out.println("错误日志数据报告：");
        System.out.println(report.toString());
    }
}