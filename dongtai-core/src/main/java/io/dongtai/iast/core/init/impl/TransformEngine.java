package io.dongtai.iast.core.init.impl;

import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.init.IEngine;
import io.dongtai.iast.core.bytecode.IastClassFileTransformer;
import io.dongtai.iast.core.service.ErrorLogReport;
import io.dongtai.log.DongTaiLog;
import org.apache.commons.lang3.time.StopWatch;

import java.lang.instrument.Instrumentation;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class TransformEngine implements IEngine {

    private Instrumentation inst;
    private IastClassFileTransformer classFileTransformer;

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
        this.classFileTransformer = IastClassFileTransformer.getInstance(inst);
        this.inst = inst;
    }

    @Override
    public void start() {
        try {
            DongTaiLog.info("Install data acquisition and analysis sub-modules");
            inst.addTransformer(classFileTransformer, true);
            classFileTransformer.reTransform();
            DongTaiLog.debug("The sub-module of data acquisition and analysis is successfully installed");
        } catch (Throwable cause) {
            DongTaiLog.error("Failed to install the sub-module of data collection and analysis");
            ErrorLogReport.sendErrorLog(cause);
        }
    }

    @Override
    public void stop() {

    }

    /**
     * Clear bytecode modifications
     */

    @Override
    public void destroy() {
        inst.removeTransformer(classFileTransformer);
        inst = null;
        classFileTransformer = null;
    }
}
