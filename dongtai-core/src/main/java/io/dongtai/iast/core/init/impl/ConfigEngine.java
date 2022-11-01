package io.dongtai.iast.core.init.impl;

import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyManager;
import io.dongtai.iast.core.init.IEngine;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.instrument.Instrumentation;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ConfigEngine implements IEngine {


    @Override
    public void init(PropertyUtils cfg, Instrumentation inst, PolicyManager policyManager) {
        DongTaiLog.debug("Initialize the core configuration of the engine");
        policyManager.loadPolicy(cfg.getPolicyPath());
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
