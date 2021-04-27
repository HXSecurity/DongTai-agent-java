package com.secnium.iast.core.engines.impl;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.ServiceFactory;
import com.secnium.iast.core.engines.IEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServiceEngine implements IEngine {
    private final Logger logger = com.secnium.iast.core.AgentEngine.DEFAULT_LOGGERCONTEXT.getLogger(getClass());
    private PropertyUtils cfg;
    private Instrumentation inst;
    private ServiceFactory serviceFactory;

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
        this.cfg = cfg;
        this.inst = inst;
        this.serviceFactory = ServiceFactory.getInstance();
        this.serviceFactory.init();
    }

    @Override
    public void start() {
        logger.info("Start the data reporting submodule");
        serviceFactory.start();
        logger.info("The data reporting submodule started successfully");
    }

    @Override
    public void stop() {
        serviceFactory.stop();
    }

    @Override
    public void destroy() {
        logger.info("Destroy the data reporting submodule");
        serviceFactory.destory();
        logger.info("The data reporting submodule is destroyed successfully");
    }
}
