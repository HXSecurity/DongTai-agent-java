package com.secnium.iast.core.engines.impl;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.ServiceFactory;
import com.secnium.iast.core.engines.IEngine;

import java.lang.instrument.Instrumentation;

import com.secnium.iast.log.DongTaiLog;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServiceEngine implements IEngine {

    private ServiceFactory serviceFactory;

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
        this.serviceFactory = ServiceFactory.getInstance();
        this.serviceFactory.init();
    }

    @Override
    public void start() {
        DongTaiLog.info("Start the data reporting submodule");
        serviceFactory.start();
        DongTaiLog.info("The data reporting submodule started successfully");
    }

    @Override
    public void stop() {
        serviceFactory.stop();
    }

    @Override
    public void destroy() {
        DongTaiLog.info("Destroy the data reporting submodule");
        serviceFactory.destory();
        DongTaiLog.info("The data reporting submodule is destroyed successfully");
    }
}
