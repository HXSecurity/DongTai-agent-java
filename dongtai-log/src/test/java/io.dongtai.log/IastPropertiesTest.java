package io.dongtai.log;

import org.junit.*;

import java.io.File;

public class IastPropertiesTest {
    private final String oldTmpPath = System.getProperty("java.io.tmpdir.dongtai");
    private final String oldLogPath = System.getProperty("dongtai.log.path");
    private final String LogLevel = System.getProperty("dongtai.log.level");

    @Before
    public void setUp() {
        clear();
    }

    @After
    public void tearDown() {
        clear();
        if (oldTmpPath != null) {
            System.setProperty("java.io.tmpdir.dongtai", oldTmpPath);
        }
        if (oldLogPath != null) {
            System.setProperty("dongtai.log.path", oldLogPath);
        }
        if (LogLevel != null) {
            System.setProperty("dongtai.log.level", LogLevel);
        }
    }

    private void clear() {
        System.clearProperty("java.io.tmpdir.dongtai");
        System.clearProperty("dongtai.log.path");
        System.clearProperty("dongtai.log.level");
    }

    @Test
    public void getLogPathTest() {
        String path;
        path = IastProperties.getLogDir();
        Assert.assertEquals("", path);

        System.setProperty("java.io.tmpdir.dongtai", File.separator + "foo");
        path = IastProperties.getLogDir();
        Assert.assertEquals(File.separator + "foo" + File.separator + "logs", path);

        clear();
        System.setProperty("java.io.tmpdir.dongtai", File.separator + "foo" + File.separator);
        path = IastProperties.getLogDir();
        Assert.assertEquals(File.separator + "foo" + File.separator + "logs", path);

        clear();
        System.setProperty("dongtai.log.path", File.separator + "foo" + File.separator);
        path = IastProperties.getLogDir();
        Assert.assertEquals(File.separator + "foo", path);
    }

    @Test
    public void getLogLevelTest() {
        //默认使用info级别
        String logLevel = IastProperties.getLogLevel();
        Assert.assertEquals("log level:" + logLevel, "info", logLevel);
        clear();
        //修改为debug级别
        System.setProperty("dongtai.log.level", "debug");
        logLevel = IastProperties.getLogLevel();
        Assert.assertEquals("log level:" + logLevel, "debug", logLevel);


    }
}
