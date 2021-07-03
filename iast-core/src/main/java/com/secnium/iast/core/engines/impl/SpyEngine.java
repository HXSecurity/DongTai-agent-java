package com.secnium.iast.core.engines.impl;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.core.enhance.asm.SpyUtils;
import org.slf4j.Logger;
import com.secnium.iast.core.util.LogUtils;

import java.lang.instrument.Instrumentation;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class SpyEngine implements IEngine {
    private final Logger logger = LogUtils.getLogger(getClass());
    private PropertyUtils cfg;
    private Instrumentation inst;

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
        this.cfg = cfg;
        this.inst = inst;
    }

    @Override
    public void start() {
        logger.info("Register spy submodule");
        SpyUtils.init(cfg.getNamespace());
        logger.info("Spy sub-module registered successfully");
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {
        logger.info("Uninstall the spy submodule");
        SpyUtils.clean(cfg.getNamespace());
        logger.info("Spy submodule uninstalled successfully");
    }
}
