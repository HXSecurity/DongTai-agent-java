package com.secnium.iast.core.engines.impl;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.IEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class SandboxEngine implements IEngine {
    private final Logger logger = com.secnium.iast.core.AgentEngine.DEFAULT_LOGGERCONTEXT.getLogger(getClass());
    private Instrumentation inst;
    private PropertyUtils cfg;

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
        this.cfg = cfg;
        this.inst = inst;
    }

    @Override
    public void start() {
        if (logger.isDebugEnabled()) {
            logger.debug("initing global control instance");
        }
        EngineManager.getInstance(cfg, inst);
        if (logger.isDebugEnabled()) {
            logger.debug("inited global control instance");
        }

    }

    @Override
    public void stop() {
        // todo: 增加额外全局开关

    }

    @Override
    public void destroy() {
        logger.info("destroy engine instance");
        EngineManager.setInstance();
        logger.info("destroy engine instance");

    }
}
