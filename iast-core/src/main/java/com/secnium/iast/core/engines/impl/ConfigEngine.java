package com.secnium.iast.core.engines.impl;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.core.handler.models.IastHookRuleModel;
import com.secnium.iast.core.report.ErrorLogReport;
import com.secnium.iast.core.util.Asserts;
import com.secnium.iast.core.util.ThrowableUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ConfigEngine implements IEngine {
    private final Logger logger = LoggerFactory.getLogger(ConfigEngine.class);
    private PropertyUtils cfg;
    private Instrumentation inst;

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
        this.cfg = cfg;
        this.inst = inst;
    }

    @Override
    public void start() {
        logger.info("Initialize the core configuration of the engine");
        IastHookRuleModel.buildModel();
        logger.info("The engine's core configuration is initialized successfully.");
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }

    public void copy(String sourceFilepath, String destFilepath) {
        InputStream initialStream = null;
        try {
            initialStream = ConfigEngine.class.getClassLoader().getResourceAsStream(sourceFilepath);
            Asserts.NOT_NULL("NoConfigFile", initialStream);
            File targetFile = new File(destFilepath);
            FileUtils.copyInputStreamToFile(initialStream, targetFile);
        } catch (IOException e) {
            ErrorLogReport.sendErrorLog
                    (ThrowableUtils.getStackTrace(e));
        }
    }
}
