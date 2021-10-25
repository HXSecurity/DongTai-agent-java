package com.secnium.iast.core.engines.impl;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.core.enhance.IastClassFileTransformer;
import com.secnium.iast.core.report.ErrorLogReport;
import com.secnium.iast.core.util.LogUtils;
import com.secnium.iast.core.util.ThrowableUtils;
import java.lang.instrument.Instrumentation;
import org.slf4j.Logger;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class TransformEngine implements IEngine {

    private final Logger logger = LogUtils.getLogger(getClass());
    private Instrumentation inst;

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
        this.inst = inst;
    }

    @Override
    public void start() {
        try {
            logger.info("Install data acquisition and analysis sub-modules");
            IastClassFileTransformer.init(inst);
            logger.info("The sub-module of data acquisition and analysis is successfully installed");
        } catch (Throwable cause) {
            logger.error("Failed to install the sub-module of data collection and analysis");
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(cause));
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
