package com.secnium.iast.core.engines.impl;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.core.util.LogUtils;
import java.lang.instrument.Instrumentation;
import org.slf4j.Logger;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class SandboxEngine implements IEngine {

    private final Logger logger = LogUtils.getLogger(getClass());
    private PropertyUtils cfg;

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
        this.cfg = cfg;
    }

    @Override
    public void start() {
        if (logger.isDebugEnabled()) {
            logger.debug("initing global control instance");
        }
        EngineManager.getInstance(cfg);
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
