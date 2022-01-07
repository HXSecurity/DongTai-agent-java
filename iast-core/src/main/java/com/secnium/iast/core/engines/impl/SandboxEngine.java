package com.secnium.iast.core.engines.impl;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.log.DongTaiLog;

import java.lang.instrument.Instrumentation;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class SandboxEngine implements IEngine {
    private Instrumentation inst;
    private PropertyUtils cfg;

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
        this.cfg = cfg;
        this.inst = inst;
    }

    @Override
    public void start() {
        if (DongTaiLog.isDebugEnabled()) {
            DongTaiLog.debug("initing global control instance");
        }
        EngineManager.getInstance(cfg, inst);
        if (DongTaiLog.isDebugEnabled()) {
            DongTaiLog.debug("inited global control instance");
        }

    }

    @Override
    public void stop() {
        // todo: 增加额外全局开关

    }

    @Override
    public void destroy() {
        DongTaiLog.info("destroy engine instance");
        EngineManager.setInstance();
        DongTaiLog.info("destroy engine instance");

    }
}
