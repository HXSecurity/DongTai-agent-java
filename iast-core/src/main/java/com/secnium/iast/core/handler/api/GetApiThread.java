package com.secnium.iast.core.handler.api;

import com.secnium.iast.core.enhance.plugins.api.spring.SpringApplicationImpl;
import com.secnium.iast.log.DongTaiLog;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static com.secnium.iast.core.report.ApiReport.sendReport;

public class GetApiThread extends Thread{

    private final Object applicationContext;

    public GetApiThread(Object applicationContext){
        this.applicationContext = applicationContext;
    }

    @Override
    public void run() {
        Map<String, Object> invoke = null;
        try {
            invoke = (Map<String, Object>) SpringApplicationImpl.getAPI.invoke(null, applicationContext);
            sendReport(invoke);
        } catch (IllegalAccessException e) {
            DongTaiLog.error(e);
        } catch (InvocationTargetException e) {
            DongTaiLog.error(e);
        } finally {
            SpringApplicationImpl.isSend = true;
        }
    }

}
