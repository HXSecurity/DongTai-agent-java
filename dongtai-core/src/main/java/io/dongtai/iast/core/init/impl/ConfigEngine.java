package io.dongtai.iast.core.init.impl;

import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.init.IEngine;
import io.dongtai.iast.core.handler.hookpoint.models.IastHookRuleModel;

import java.lang.instrument.Instrumentation;

import io.dongtai.log.DongTaiLog;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ConfigEngine implements IEngine {


    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
        DongTaiLog.debug("Initialize the core configuration of the engine");
        IastHookRuleModel.buildModel();
        DongTaiLog.debug("The engine's core configuration is initialized successfully.");
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }

}
