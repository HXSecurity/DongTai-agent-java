package io.dongtai.iast.core.handler.hookpoint.vulscan;

import org.json.JSONObject;
import org.junit.Test;

public class ReportConstantTest {
    @Test
    public void generateHeartBeatReport() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_HEART_BEAT);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.PID, 20102);
        detail.put(ReportConstant.NETWORK, 20102);
        detail.put(ReportConstant.MEMORY, 20102);
        detail.put(ReportConstant.CPU, 20102);
        detail.put(ReportConstant.DISK, 20102);
        detail.put(ReportConstant.REQ_COUNT, 20102);
        detail.put(ReportConstant.CONTAINER_NAME, 20102);
        detail.put(ReportConstant.CONTAINER_VERSION, 20102);
        detail.put(ReportConstant.SERVER_PATH, 20102);
        detail.put(ReportConstant.SERVER_ADDR, 20102);
        detail.put(ReportConstant.SERVER_PORT, 20102);

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

        detail.put(ReportConstant.PROTOCOL, 20102);
        detail.put(ReportConstant.SCHEME, 20102);
        detail.put(ReportConstant.METHOD, 20102);
        detail.put(ReportConstant.URL, 20102);
        detail.put(ReportConstant.QUERY_STRING, 20102);
        detail.put(ReportConstant.REQ_BODY, 20102);
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

        detail.put(ReportConstant.PROTOCOL, 20102);
        detail.put(ReportConstant.SCHEME, 20102);
        detail.put(ReportConstant.METHOD, 20102);
        detail.put(ReportConstant.URL, 20102);
        detail.put(ReportConstant.QUERY_STRING, 20102);
        detail.put(ReportConstant.REQ_BODY, 20102);
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

        detail.put(ReportConstant.PROTOCOL, 20102);
        detail.put(ReportConstant.SCHEME, 20102);
        detail.put(ReportConstant.METHOD, 20102);
        detail.put(ReportConstant.URL, 20102);
        detail.put(ReportConstant.QUERY_STRING, 20102);
        detail.put(ReportConstant.REQ_BODY, 20102);
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

        detail.put(ReportConstant.PROTOCOL, 20102);
        detail.put(ReportConstant.SCHEME, 20102);
        detail.put(ReportConstant.METHOD, 20102);
        detail.put(ReportConstant.URL, 20102);
        detail.put(ReportConstant.QUERY_STRING, 20102);
        detail.put(ReportConstant.REQ_BODY, 20102);
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

        detail.put(ReportConstant.PROTOCOL, 20102);
        detail.put(ReportConstant.SCHEME, 20102);
        detail.put(ReportConstant.METHOD, 20102);
        detail.put(ReportConstant.URL, 20102);
        detail.put(ReportConstant.QUERY_STRING, 20102);
        detail.put(ReportConstant.REQ_BODY, 20102);
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

        detail.put(ReportConstant.PROTOCOL, 20102);
        detail.put(ReportConstant.SCHEME, 20102);
        detail.put(ReportConstant.METHOD, 20102);
        detail.put(ReportConstant.URL, 20102);
        detail.put(ReportConstant.QUERY_STRING, 20102);
        detail.put(ReportConstant.REQ_BODY, 20102);
        detail.put(ReportConstant.VULN_CALLER, 20102);

        System.out.println("错误日志数据报告：");
        System.out.println(report.toString());
    }
}