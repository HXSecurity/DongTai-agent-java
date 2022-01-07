package com.secnium.iast.core.engines.impl;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.core.handler.models.IastHookRuleModel;

import java.lang.instrument.Instrumentation;

import com.secnium.iast.log.DongTaiLog;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ConfigEngine implements IEngine {


    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
    }

    @Override
    public void start() {
        DongTaiLog.info("Initialize the core configuration of the engine");
        IastHookRuleModel.buildModel();
        DongTaiLog.info("The engine's core configuration is initialized successfully.");
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }

}
