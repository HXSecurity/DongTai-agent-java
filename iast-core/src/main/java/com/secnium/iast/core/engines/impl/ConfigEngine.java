package com.secnium.iast.core.engines.impl;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.core.handler.models.IastHookRuleModel;
import com.secnium.iast.core.util.LogUtils;
import java.lang.instrument.Instrumentation;
import org.slf4j.Logger;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ConfigEngine implements IEngine {

    private final Logger logger = LogUtils.getLogger(ConfigEngine.class);

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
    }

    @Override
    public void start() {
        logger.info("Initialize the core configuration of the engine");
        IastHookRuleModel.buildModel();
        logger.info("The engine's core configuration is initialized successfully.");
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }

}
