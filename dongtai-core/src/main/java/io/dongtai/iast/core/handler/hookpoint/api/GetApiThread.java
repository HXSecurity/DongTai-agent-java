package io.dongtai.iast.core.handler.hookpoint.api;

import io.dongtai.iast.core.bytecode.enhance.plugin.spring.SpringApplicationImpl;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.util.Map;

public class GetApiThread extends Thread {

    private final Object applicationContext;

    public GetApiThread(Object applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run() {
        try {
            if (SpringApplicationImpl.getAPI == null) {
                return;
            }
            Map<String, Object> invoke = null;

            invoke = (Map<String, Object>) SpringApplicationImpl.getAPI.invoke(null, applicationContext);
            ApiReport.sendReport(invoke);
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("API_COLLECTOR_GET_API_THREAD_EXECUTE_FAILED"), e);
        } finally {
            SpringApplicationImpl.isSend = true;
            SpringApplicationImpl.getAPI = null;
        }
    }

}