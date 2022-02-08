package com.secnium.iast.core.engines.impl;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.core.enhance.IastClassFileTransformer;
import com.secnium.iast.core.report.ErrorLogReport;
import com.secnium.iast.log.DongTaiLog;
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
            DongTaiLog.info("The sub-module of data acquisition and analysis is successfully installed");
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
