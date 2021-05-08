package com.secnium.iast.core.engines.impl;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.core.util.NamespaceConvert;
import org.slf4j.Logger;
import com.secnium.iast.core.util.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class LoggerEngine implements IEngine {
    private PropertyUtils cfg;
    private Instrumentation inst;

    public LoggerEngine() {
        this.configLogger();
    }

    private void configLogger() {
        final LoggerContext loggerContext = new LoggerContext();
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        loggerContext.reset();
        final Logger logger = LogUtils.getLogger(LoggerEngine.class);
        InputStream configStream = null;
        try {
            NamespaceConvert.initNamespaceConvert("DongTai");
            configStream = LoggerEngine.class.getClassLoader().getResourceAsStream("logback-dongtai.xml");
            configurator.doConfigure(configStream);
            logger.info("Log module initialized successfully");
        } catch (JoranException e) {
            e.printStackTrace();
        } finally {
            if (configStream != null) {
                try {
                    configStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
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
