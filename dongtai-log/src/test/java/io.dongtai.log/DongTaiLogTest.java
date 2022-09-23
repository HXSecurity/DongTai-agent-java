package io.dongtai.log;

import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class DongTaiLogTest {
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final boolean oldEnableColor = DongTaiLog.enableColor;
    private final static String LS = System.getProperty("line.separator");
    private static final String TITLE = "[io.dongtai.iast.agent] ";

    @Before
    public void setUp() {
        DongTaiLog.enablePrintLog = true;
        DongTaiLog.enableColor = false;
        clear();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @After
    public void tearDown() {
        DongTaiLog.enablePrintLog = false;
        DongTaiLog.enableColor = oldEnableColor;
        clear();
        System.setOut(standardOut);
    }

    private void clear() {
        outputStreamCaptor.reset();
    }

    @Test
    public void canLogTest() {
        DongTaiLog.enablePrintLog = true;
        DongTaiLog.setLevel(DongTaiLog.LogLevel.ERROR);
        Assert.assertTrue(DongTaiLog.canLog(DongTaiLog.LogLevel.ERROR));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.WARN));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.INFO));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.DEBUG));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.TRACE));

        DongTaiLog.setLevel(DongTaiLog.LogLevel.WARN);
        Assert.assertTrue(DongTaiLog.canLog(DongTaiLog.LogLevel.ERROR));
        Assert.assertTrue(DongTaiLog.canLog(DongTaiLog.LogLevel.WARN));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.INFO));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.DEBUG));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.TRACE));

        DongTaiLog.setLevel(DongTaiLog.LogLevel.INFO);
        Assert.assertTrue(DongTaiLog.canLog(DongTaiLog.LogLevel.ERROR));
        Assert.assertTrue(DongTaiLog.canLog(DongTaiLog.LogLevel.WARN));
        Assert.assertTrue(DongTaiLog.canLog(DongTaiLog.LogLevel.INFO));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.DEBUG));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.TRACE));

        DongTaiLog.setLevel(DongTaiLog.LogLevel.DEBUG);
        Assert.assertTrue(DongTaiLog.canLog(DongTaiLog.LogLevel.ERROR));
        Assert.assertTrue(DongTaiLog.canLog(DongTaiLog.LogLevel.WARN));
        Assert.assertTrue(DongTaiLog.canLog(DongTaiLog.LogLevel.INFO));
        Assert.assertTrue(DongTaiLog.canLog(DongTaiLog.LogLevel.DEBUG));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.TRACE));

        DongTaiLog.setLevel(DongTaiLog.LogLevel.TRACE);
        Assert.assertTrue(DongTaiLog.canLog(DongTaiLog.LogLevel.ERROR));
        Assert.assertTrue(DongTaiLog.canLog(DongTaiLog.LogLevel.WARN));
        Assert.assertTrue(DongTaiLog.canLog(DongTaiLog.LogLevel.INFO));
        Assert.assertTrue(DongTaiLog.canLog(DongTaiLog.LogLevel.DEBUG));
        Assert.assertTrue(DongTaiLog.canLog(DongTaiLog.LogLevel.TRACE));

        DongTaiLog.enablePrintLog = false;
        DongTaiLog.setLevel(DongTaiLog.LogLevel.TRACE);
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.ERROR));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.WARN));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.INFO));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.DEBUG));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.TRACE));
    }

    @Test
    public void logTest() {
        DongTaiLog.setLevel(DongTaiLog.LogLevel.TRACE);
        clear();
        DongTaiLog.trace("foo");
        Assert.assertTrue("TRACE log", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[TRACE] foo" + LS));
        clear();
        DongTaiLog.trace("foo {} {}", "bar", "baz");
        Assert.assertTrue("TRACE log format", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[TRACE] foo bar baz" + LS));
        clear();
        DongTaiLog.trace("foo", new Exception("bar"));
        Assert.assertTrue("TRACE log message with exception", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[TRACE] foo, Exception: java.lang.Exception: bar" + LS));
        clear();
        DongTaiLog.trace(new Exception("bar"));
        Assert.assertTrue("TRACE log exception", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[TRACE] Exception: java.lang.Exception: bar" + LS));

        DongTaiLog.setLevel(DongTaiLog.LogLevel.DEBUG);
        clear();
        DongTaiLog.debug("foo");
        Assert.assertTrue("DEBUG log", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[DEBUG] foo" + LS));
        clear();
        DongTaiLog.debug("foo {} {}", "bar", "baz");
        Assert.assertTrue("DEBUG log format", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[DEBUG] foo bar baz" + LS));
        clear();
        DongTaiLog.debug("foo", new Exception("bar"));
        Assert.assertTrue("DEBUG log message with exception", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[DEBUG] foo, Exception: java.lang.Exception: bar" + LS));
        clear();
        DongTaiLog.debug(new Exception("bar"));
        Assert.assertTrue("DEBUG log exception", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[DEBUG] Exception: java.lang.Exception: bar" + LS));

        DongTaiLog.setLevel(DongTaiLog.LogLevel.INFO);
        clear();
        DongTaiLog.info("foo");
        Assert.assertTrue("INFO log", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[INFO] foo" + LS));
        clear();
        DongTaiLog.info("foo {} {}", "bar", "baz");
        Assert.assertTrue("INFO log format", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[INFO] foo bar baz" + LS));
        clear();
        DongTaiLog.info("foo", new Exception("bar"));
        Assert.assertTrue("INFO log message with exception", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[INFO] foo, Exception: java.lang.Exception: bar" + LS));
        clear();
        DongTaiLog.info(new Exception("bar"));
        Assert.assertTrue("INFO log exception", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[INFO] Exception: java.lang.Exception: bar" + LS));

        DongTaiLog.setLevel(DongTaiLog.LogLevel.WARN);
        clear();
        DongTaiLog.warn("foo");
        Assert.assertTrue("WARN log", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[WARN] foo" + LS));
        clear();
        DongTaiLog.warn("foo {} {}", "bar", "baz");
        Assert.assertTrue("WARN log format", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[WARN] foo bar baz" + LS));
        clear();
        DongTaiLog.warn("foo", new Exception("bar"));
        Assert.assertTrue("WARN log message with exception", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[WARN] foo, Exception: java.lang.Exception: bar" + LS));
        clear();
        DongTaiLog.warn(new Exception("bar"));
        Assert.assertTrue("WARN log exception", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[WARN] Exception: java.lang.Exception: bar" + LS));

        DongTaiLog.setLevel(DongTaiLog.LogLevel.ERROR);
        clear();
        DongTaiLog.error("foo");
        Assert.assertTrue("ERROR log", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[ERROR] foo" + LS));
        clear();
        DongTaiLog.error("foo {} {}", "bar", "baz");
        Assert.assertTrue("ERROR log format", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[ERROR] foo bar baz" + LS));
        clear();
        DongTaiLog.error("foo", new Exception("bar"));
        Assert.assertTrue("ERROR log message with exception", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[ERROR] foo, Exception: java.lang.Exception: bar" + LS));
        clear();
        DongTaiLog.error(new Exception("bar"));
        Assert.assertTrue("ERROR log exception", outputStreamCaptor.toString()
                .endsWith(" " + TITLE + "[ERROR] Exception: java.lang.Exception: bar" + LS));
    }
}
