package com.secnium.iast.core.engines.impl;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.ServiceFactory;
import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.core.util.LogUtils;
import java.lang.instrument.Instrumentation;
import org.slf4j.Logger;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServiceEngine implements IEngine {

    private final Logger logger = LogUtils.getLogger(getClass());
    private ServiceFactory serviceFactory;

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
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
