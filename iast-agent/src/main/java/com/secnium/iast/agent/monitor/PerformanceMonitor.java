package com.secnium.iast.agent.monitor;

import com.secnium.iast.agent.AgentRegister;
import com.secnium.iast.agent.IastProperties;
import com.secnium.iast.agent.LogUtils;
import com.secnium.iast.agent.manager.EngineManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * 负责监控jvm性能状态，如果达到停止阈值，则停止检测引擎；如果达到卸载阈值，则卸载引擎；
 *
 * @author dongzhiyong@huoxian.cn
 */
public class PerformanceMonitor implements IMonitor {
    private final static IastProperties PROPERTIES = IastProperties.getInstance();
    private final static String TOKEN = PROPERTIES.getIastServerToken();
    private final static String START_URL = PROPERTIES.getBaseUrl() + "/api/v1/agent/limit";
    private final static String AGENT_TOKEN = URLEncoder.encode(AgentRegister.getAgentToken());
    private static Integer AGENT_THRESHOLD_VALUE;

    private final EngineManager engineManager;

    public PerformanceMonitor(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    public double memUsedRate() {
        double free = (double) Runtime.getRuntime().freeMemory();
        double max = (double) Runtime.getRuntime().maxMemory();
        return free / max;
    }

    public Integer getCpuUsedRate() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        return (int) (operatingSystemMXBean.getSystemLoadAverage() * 10);
    }

    public static Integer checkThresholdValue() {
        try {
            String respRaw = getThresholdValue();
            if (respRaw != null && !respRaw.isEmpty()) {
                JSONObject resp = new JSONObject(respRaw);
                JSONArray limitArray = (JSONArray) resp.get("data");
                JSONObject cpuLimit = (JSONObject) limitArray.get(0);
                return Integer.valueOf(cpuLimit.get("value").toString());
            }
        } catch (Exception e) {
            return 60;
        }
        return 60;
    }

    /**
     * 是否到达停止引擎的阈值
     * // 前置状态：0
     * // 切换状态：1
     *
     * @return true, 需要停止；false - 不需要停止
     */
    public boolean isStop(double UsedRate, int preStatus) {
        return UsedRate > AGENT_THRESHOLD_VALUE && (preStatus == 0);
    }

    /**
     * 是否到达启动引擎的阈值
     * // 前置状态：1
     * // 切换状态：0
     *
     * @return true, 需要启动；false - 不需要启动
     */
    public boolean isStart(double UsedRate, int preStatus) {
        return UsedRate < AGENT_THRESHOLD_VALUE && (preStatus == 1);
    }

    /**
     * 状态发生转换时，触发engineManager的操作
     * <p>
     * 状态维护：
     * 0 -> 1 -> 0
     */
    @Override
    public void check() {
        PerformanceMonitor.AGENT_THRESHOLD_VALUE = PerformanceMonitor.checkThresholdValue();
        double UsedRate = getCpuUsedRate();
        int preStatus = this.engineManager.getRunningStatus();
        if (isStart(UsedRate, preStatus)) {
            this.engineManager.start();
            this.engineManager.setRunningStatus(0);
            LogUtils.info("当前CPU使用率为" + UsedRate + "，低于阈值" + AGENT_THRESHOLD_VALUE + "%，检测引擎启动");
        } else if (isStop(UsedRate, preStatus)) {
            this.engineManager.stop();
            this.engineManager.setRunningStatus(1);
            LogUtils.info("当前CPU使用率为" + UsedRate + "，高于阈值" + AGENT_THRESHOLD_VALUE + "%，检测引擎停止");
        }
    }

    private static String getThresholdValue() {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        String result = null;
        try {
            URL url = new URL(PerformanceMonitor.START_URL + "?agentName=" + AGENT_TOKEN);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-type", "application/json; charset=utf-8");
            connection.setRequestProperty("Authorization", "Token " + TOKEN);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder sbf = new StringBuilder();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                }
                result = sbf.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
            if (null != connection) {
                connection.disconnect();
            }
        }
        return result;
    }

    static {
        AGENT_THRESHOLD_VALUE = 60;
    }
}
