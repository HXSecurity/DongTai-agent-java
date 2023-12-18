package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.HttpClientUtils;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.common.config.ConfigBuilder;
import io.dongtai.iast.common.config.ConfigKey;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.iast.common.utils.limit.InterfaceRateLimiterUtil;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.util.HashMap;
import java.util.Map;

public class ConfigMonitor implements IMonitor {
    private static final String NAME = "ConfigMonitor";

    @Override
    public String getName() {
        return AgentConstant.THREAD_NAME_PREFIX + NAME;
    }

    @Override
    public void check() {
        try {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("agent_id", String.valueOf(AgentRegisterReport.getAgentId()));

            StringBuilder response = HttpClientUtils.sendGet(ApiPath.AGENT_CONFIG, parameters);
            ConfigBuilder.getInstance().updateFromRemote(response.toString());

            updateConfig();
        } catch (Throwable t) {
            DongTaiLog.warn(ErrorCode.AGENT_MONITOR_THREAD_CHECK_FAILED, t);
        }
    }

    private void updateConfig() {
        Boolean enableLog = ConfigBuilder.getInstance().get(ConfigKey.ENABLE_LOGGER);
        if (enableLog != null) {
            DongTaiLog.ENABLED = enableLog;
        }

        String logLevel = ConfigBuilder.getInstance().get(ConfigKey.LOGGER_LEVEL);
        if (logLevel != null) {
            DongTaiLog.setLevel(DongTaiLog.parseLevel(logLevel));
        }

        //获取是否开启qps限流
        Boolean enableQpsRate = ConfigBuilder.getInstance().get(ConfigKey.ENABLE_QPS_RATE_LIMIT);
        if (enableQpsRate) {
            int qpsRateLimit = ConfigBuilder.getInstance().get(ConfigKey.QPS_RATE_LIMIT);
            if (qpsRateLimit <= 0){
                DongTaiLog.error("qpsRateLimit the value cannot be less than 0");
                qpsRateLimit =  100;
                DongTaiLog.error("qpsRateLimit revert to 100");

            }
            int tokenBucketPoolSize = ConfigBuilder.getInstance().get(ConfigKey.TOKEN_BUCKET_POOL_SIZE);
            if (tokenBucketPoolSize <= 0){
                DongTaiLog.error("tokenBucketPoolSize the value cannot be less than 0");
                tokenBucketPoolSize=5000;
                DongTaiLog.error("tokenBucketPoolSize revert to 5000");

            }
            //判断是否已经开启，如果已经开启，则更新数据
            if (InterfaceRateLimiterUtil.getRateLimiterState()) {
                InterfaceRateLimiterUtil.updateTheData(qpsRateLimit, tokenBucketPoolSize);
            } else {
                //初始化令牌池工具，设置池大小和速率
                InterfaceRateLimiterUtil.initializeInstance(qpsRateLimit, tokenBucketPoolSize);
            }
        }else {
            if(InterfaceRateLimiterUtil.getRateLimiterState()){
                InterfaceRateLimiterUtil.turnOffTheRateLimiter();
            }
        }

    }

    @Override
    public void run() {
        try {
            while (!MonitorDaemonThread.isExit) {
                this.check();
                ThreadUtils.threadSleep(60);
            }
        } catch (Throwable t) {
            DongTaiLog.debug("{} interrupted: {}", getName(), t.getMessage());
        }
    }
}
