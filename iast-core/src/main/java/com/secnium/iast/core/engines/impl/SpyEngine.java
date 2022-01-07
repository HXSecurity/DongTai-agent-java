package com.secnium.iast.core.engines.impl;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.core.enhance.asm.SpyUtils;

import java.lang.instrument.Instrumentation;

import com.secnium.iast.log.DongTaiLog;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class SpyEngine implements IEngine {

    private PropertyUtils cfg;

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
        this.cfg = cfg;
    }

    @Override
    public void start() {
        DongTaiLog.info("Register spy submodule");
        SpyUtils.init(cfg.getNamespace());
        DongTaiLog.info("Spy sub-module registered successfully");
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {
        DongTaiLog.info("Uninstall the spy submodule");
        SpyUtils.clean(cfg.getNamespace());
        DongTaiLog.info("Spy submodule uninstalled successfully");
    }
}
