package com.secnium.iast.core.engines.impl;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.core.enhance.IastClassFileTransformer;
import com.secnium.iast.core.report.ErrorLogReport;

import java.lang.instrument.Instrumentation;

import com.secnium.iast.log.DongTaiLog;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class TransformEngine implements IEngine {

    private Instrumentation inst;

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
        this.inst = inst;
    }

    @Override
    public void start() {
        try {
            DongTaiLog.info("Install data acquisition and analysis sub-modules");
            IastClassFileTransformer.init(inst);
            DongTaiLog.info("The sub-module of data acquisition and analysis is successfully installed");
        } catch (Throwable cause) {
            DongTaiLog.error("Failed to install the sub-module of data collection and analysis");
            ErrorLogReport.sendErrorLog(cause);
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {
        IastClassFileTransformer.release(inst);
    }
}
