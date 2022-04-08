package io.dongtai.iast.core.service;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import io.dongtai.iast.core.utils.Constants;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

import java.io.File;

public class ServiceDirReport {

    private String serviceDir;
    private String serviceType;
    private StringBuilder dirStringBuilder = new StringBuilder();


    public ServiceDirReport() {
    }

    public String getServereAddressMsg() {
        if (new File("/var/run/secrets/kubernetes.io").exists()){
            serviceType = "k8s";
        }else if (new File("/.dockerenv").exists()){
            serviceType = "docker";
        }else {
            serviceType = "virtual";
        }
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_SERVICE_DIR);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_ID, EngineManager.getAgentId());
        detail.put(ReportConstant.SERVICE_DIR, this.serviceDir);
        detail.put(ReportConstant.SERVICE_TYPE, this.serviceType);

        return report.toString();
    }

    public void send() {
        try {
            serviceDir = genDirTree(getWebServerPath(),0,"");
            ThreadPools.sendReport(Constants.API_REPORT_UPLOAD, this.getServereAddressMsg());
        } catch (Exception e) {
            DongTaiLog.error("send ServiceDir to {} error, reason: {}", Constants.API_REPORT_UPLOAD, e);
        }
    }

    public String genDirTree(String path, int level, String dir) {
        level++;
        File file = new File(path);
        File[] files = file.listFiles();
        if (!file.exists()) {
            return null;
        }
        if (files.length != 0) {
            for (File f : files) {
                if (f.isDirectory()) {
                    dir = f.getName();
                    genDirTree(f.getAbsolutePath(), level, dir);
                } else {
                    dirStringBuilder.append(f.getAbsolutePath()).append("\n");
                }
            }
        }
        return dirStringBuilder.toString();
    }

    public static String getWebServerPath() {
        File file = new File(".");
        String path = file.getAbsolutePath();
        return path.substring(0, path.length() - 2);
    }

}
