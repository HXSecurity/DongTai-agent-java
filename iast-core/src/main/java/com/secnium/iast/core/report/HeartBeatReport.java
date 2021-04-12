package com.secnium.iast.core.report;

import com.secnium.iast.core.AbstractThread;
import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.middlewarerecognition.IServer;
import com.secnium.iast.core.middlewarerecognition.ServerDetect;
import com.secnium.iast.core.util.ByteUtils;
import com.secnium.iast.core.util.base64.Base64Utils;
import org.json.JSONObject;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.net.*;
import java.util.Enumeration;

/**
 * 心跳机制实现，默认30s
 *
 * @author dongzhiyong@huoxian.cn
 */
public class HeartBeatReport extends AbstractThread {
    final ServerDetect SERVER_DETECT = ServerDetect.getInstance();
    final IServer SERVER = SERVER_DETECT.getWebserver();
    final MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
    final String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    final String hostName = getInternalHostName();
    final static String CURRENT_PATH = getCurrentPath();
    private static HeartBeatReport instance;

    public HeartBeatReport(long waitTime) {
        super(null, true, waitTime);
    }

    public static HeartBeatReport getInstance() {
        return instance;
    }

    public static HeartBeatReport getInstance(long waitTime) {
        if (null == instance) {
            instance = new HeartBeatReport(waitTime);
        }
        return instance;
    }

    @Override
    protected void send() {
        String msg = generateHeartBeatMsg();
        EngineManager.sendNewReport(msg);
    }

    private String generateHeartBeatMsg() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_HEART_BEAT);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.SERVER_ENV, Base64Utils.encodeBase64String(System.getProperties().toString().getBytes()).replaceAll("\n", ""));
        detail.put(ReportConstant.AGENT_NAME, AgentRegisterReport.getAgentToken());
        detail.put(ReportConstant.LANGUAGE, ReportConstant.LANGUAGE_VALUE);
        detail.put(ReportConstant.HEART_BEAT_PID, getPid());
        detail.put(ReportConstant.HOSTNAME, getHostName());
        detail.put(ReportConstant.HEART_BEAT_NETWORK, readIpInfo());
        detail.put(ReportConstant.HEART_BEAT_MEMORY, readMemInfo());
        detail.put(ReportConstant.HEART_BEAT_CPU, readCpuInfo());
        detail.put(ReportConstant.HEART_BEAT_DISK, getDiskInfo());
        detail.put(ReportConstant.HEART_BEAT_REQ_COUNT, EngineManager.getRequestCount());
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_NAME, SERVER == null ? "" : SERVER.getName());
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_VERSION, SERVER == null ? "" : SERVER.getVersion());
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_PATH, SERVER_DETECT.getWebServerPath());
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_HOSTNAME, getHostName());
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_IP, null != EngineManager.SERVER ? EngineManager.SERVER.getServerAddr() : "");
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_PORT, null != EngineManager.SERVER ? EngineManager.SERVER.getServerPort() : "");

        return report.toString();
    }

    public static String getWebServerPath() {
        if (null == instance) {
            return CURRENT_PATH;
        } else {
            return instance.SERVER_DETECT.getWebServerPath();
        }
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

    public String getHostName() {
        return hostName;
    }

    public static String readIpInfo() {
        try {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            Enumeration interefaces = NetworkInterface.getNetworkInterfaces();
            while (interefaces.hasMoreElements()) {
                NetworkInterface iface = (NetworkInterface) interefaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                Enumeration addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) addresses.nextElement();
                    if (inetAddress instanceof Inet6Address) {
                        continue;
                    }
                    if (first) {
                        sb.append("{\"name\"").append(":").append("\"").append(iface.getDisplayName()).append("\"");
                        sb.append(",\"ip\"").append(":").append("\"").append(inetAddress.getHostAddress()).append("\"}");
                        first = false;
                    } else {
                        sb.append(",{\"name\"").append(":").append("\"").append(iface.getDisplayName()).append("\"");
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
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"total\":").append("\"0.13 GB\"");
        sb.append(",\"use\":").append("\"0.03 GB\"");
        sb.append(",\"rate\":").append("\"3.21 %\"");
        sb.append("}");
        return sb.toString();
    }

    /**
     * 获取JVM相关的内存
     */
    public String readMemInfo() {
        JSONObject memoryReport = new JSONObject();
        memoryReport.put("total", ByteUtils.formatByteSize(memoryUsage.getMax()));
        memoryReport.put("use", ByteUtils.formatByteSize(memoryUsage.getUsed()));
        memoryReport.put("rate", memoryUsage.getUsed() / memoryUsage.getMax());
        return memoryReport.toString();
    }

    public static String getDiskInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append("\"total\"").append(":").append("\"").append("\"")
                .append(",\"use\"").append(":").append("\"").append("\"")
                .append(",\"rate\"").append(":").append("\"").append("\"");
        sb.append("}");
        return sb.toString();
    }

    public String getPid() {
        return pid;
    }

    public static String getIpAddr() {
        try {
            InetAddress ip4 = Inet4Address.getLocalHost();
            return ip4.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }
}
