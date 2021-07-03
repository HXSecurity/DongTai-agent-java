package com.secnium.iast.core.report;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.middlewarerecognition.IServer;
import com.secnium.iast.core.middlewarerecognition.ServerDetect;
import com.secnium.iast.core.util.ByteUtils;
import com.secnium.iast.core.util.base64.Base64Encoder;
import org.json.JSONObject;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.net.*;
import java.util.Enumeration;

/**
 * 心跳机制实现，默认30s
 *
 * @author dongzhiyong@huoxian.cn
 */
public class HeartBeatReport {
    final static ServerDetect SERVER_DETECT = ServerDetect.getInstance();
    final static IServer SERVER = SERVER_DETECT.getWebserver();
    final static String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    final static String HOST_NAME = getInternalHostName();


    public static void createReport() {
        String msg = generateHeartBeatMsg();
        EngineManager.sendNewReport(msg);
    }

    private static String generateHeartBeatMsg() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_HEART_BEAT);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.SERVER_ENV, Base64Encoder.encodeBase64String(System.getProperties().toString().getBytes()).replaceAll("\n", ""));
        detail.put(ReportConstant.AGENT_NAME, AgentRegisterReport.getAgentToken());
        detail.put(ReportConstant.PROJECT_NAME, AgentRegisterReport.getProjectName());
        detail.put(ReportConstant.LANGUAGE, ReportConstant.LANGUAGE_VALUE);
        detail.put(ReportConstant.HEART_BEAT_PID, PID);
        detail.put(ReportConstant.HOSTNAME, HOST_NAME);
        detail.put(ReportConstant.HEART_BEAT_NETWORK, readIpInfo());
        detail.put(ReportConstant.HEART_BEAT_MEMORY, readMemInfo());
        detail.put(ReportConstant.HEART_BEAT_CPU, readCpuInfo());
        detail.put(ReportConstant.HEART_BEAT_DISK, getDiskInfo());
        detail.put(ReportConstant.HEART_BEAT_REQ_COUNT, EngineManager.getRequestCount());
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_NAME, SERVER == null ? "" : SERVER.getName());
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_VERSION, SERVER == null ? "" : SERVER.getVersion());
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_PATH, SERVER_DETECT.getWebServerPath());
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_HOSTNAME, HOST_NAME);
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_IP, null != EngineManager.SERVER ? EngineManager.SERVER.getServerAddr() : "");
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_PORT, null != EngineManager.SERVER ? EngineManager.SERVER.getServerPort() : "");

        return report.toString();
    }

    public static String getWebServerPath() {
        return SERVER_DETECT.getWebServerPath();
    }

    /**
     * 获取当前目录
     *
     * @return 当前目录的绝对路径
     */
    public static String getCurrentPath() {
        String absolutePath = new File(".").getAbsolutePath();
        return absolutePath.substring(0, absolutePath.length() - 2);
    }

    /**
     * 获取linux主机名
     *
     * @return 主机名
     */
    private static String getHostNameForLinux() {
        try {
            return (InetAddress.getLocalHost()).getHostName();
        } catch (UnknownHostException uhe) {
            String host = uhe.getMessage();
            if (host != null) {
                int colon = host.indexOf(':');
                if (colon > 0) {
                    return host.substring(0, colon);
                }
            }
            return "UnknownHost";
        }
    }

    /**
     * 获取主机名
     *
     * @return 主机名
     */
    public static String getInternalHostName() {
        if (System.getenv("COMPUTERNAME") != null) {
            return System.getenv("COMPUTERNAME");
        } else {
            return getHostNameForLinux();
        }
    }

    private static String readIpInfo() {
        try {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            Enumeration<?> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<?> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) addresses.nextElement();
                    if (inetAddress instanceof Inet6Address) {
                        continue;
                    }
                    if (first) {
                        sb.append("{\"name\"").append(":").append("\"").append(networkInterface.getDisplayName()).append("\"");
                        sb.append(",\"ip\"").append(":").append("\"").append(inetAddress.getHostAddress()).append("\"}");
                        first = false;
                    } else {
                        sb.append(",{\"name\"").append(":").append("\"").append(networkInterface.getDisplayName()).append("\"");
                        sb.append(",\"ip\"").append(":").append("\"").append(inetAddress.getHostAddress()).append("\"}");
                    }
                }
            }
            return sb.toString();
        } catch (SocketException e) {
            return "{}";
        }
    }

    public static String readCpuInfo() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        JSONObject cpuInfo = new JSONObject();
        cpuInfo.put("rate", (int) (operatingSystemMXBean.getSystemLoadAverage() * 10));
        return cpuInfo.toString();
    }

    /**
     * 获取JVM相关的内存
     */
    public static String readMemInfo() {
        MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        JSONObject memoryReport = new JSONObject();
        memoryReport.put("total", ByteUtils.formatByteSize(memoryUsage.getMax()));
        memoryReport.put("use", ByteUtils.formatByteSize(memoryUsage.getUsed()));
        memoryReport.put("rate", memoryUsage.getUsed() / memoryUsage.getMax());
        return memoryReport.toString();
    }

    public static String getDiskInfo() {
        return "{}";
    }
}
