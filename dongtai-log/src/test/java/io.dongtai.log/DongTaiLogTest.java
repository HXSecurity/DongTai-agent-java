package io.dongtai.log;

import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class DongTaiLogTest {
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final boolean oldEnableColor = DongTaiLog.ENABLE_COLOR;
    private final static String LS = System.getProperty("line.separator");
    private static final String TITLE = "[io.dongtai.iast.agent] ";

    @Before
    public void setUp() {
        DongTaiLog.ENABLED = true;
        DongTaiLog.ENABLE_COLOR = false;
        clear();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @After
    public void tearDown() {
        DongTaiLog.ENABLED = false;
        DongTaiLog.ENABLE_COLOR = oldEnableColor;
        clear();
        System.setOut(standardOut);
    }

    private void clear() {
        outputStreamCaptor.reset();
    }

    @Test
    public void canLogTest() {
        DongTaiLog.ENABLED = true;
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

        DongTaiLog.ENABLED = false;
        DongTaiLog.setLevel(DongTaiLog.LogLevel.TRACE);
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.ERROR));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.WARN));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.INFO));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.DEBUG));
        Assert.assertFalse(DongTaiLog.canLog(DongTaiLog.LogLevel.TRACE));
    }

    @Test
    public void logTest() {
        DongTaiLog.setLevel(DongTaiLog.LogLevel.DEBUG);
        clear();
        DongTaiLog.trace("foo");
        Assert.assertEquals("DEBUG level ignore", outputStreamCaptor.toString(), "");

        DongTaiLog.setLevel(DongTaiLog.LogLevel.INFO);
        clear();
        DongTaiLog.trace("foo");
        Assert.assertEquals("INFO level ignore", outputStreamCaptor.toString(), "");
        clear();
        DongTaiLog.debug("foo");
        Assert.assertEquals("INFO level ignore", outputStreamCaptor.toString(), "");

        DongTaiLog.setLevel(DongTaiLog.LogLevel.WARN);
        clear();
        DongTaiLog.trace("foo");
        Assert.assertEquals("WARN level ignore", outputStreamCaptor.toString(), "");
        clear();
        DongTaiLog.debug("foo");
        Assert.assertEquals("WARN level ignore", outputStreamCaptor.toString(), "");
        clear();
        DongTaiLog.info("foo");
        Assert.assertEquals("WARN level ignore", outputStreamCaptor.toString(), "");

        DongTaiLog.setLevel(DongTaiLog.LogLevel.ERROR);
        clear();
        DongTaiLog.trace("foo");
        Assert.assertEquals("ERROR level ignore", outputStreamCaptor.toString(), "");
        clear();
        DongTaiLog.debug("foo");
        Assert.assertEquals("ERROR level ignore", outputStreamCaptor.toString(), "");
        clear();
        DongTaiLog.info("foo");
        Assert.assertEquals("ERROR level ignore", outputStreamCaptor.toString(), "");
        clear();
        DongTaiLog.warn("foo");
        Assert.assertEquals("ERROR level ignore", outputStreamCaptor.toString(), "");

        DongTaiLog.setLevel(DongTaiLog.LogLevel.TRACE);
        clear();
        DongTaiLog.trace("foo");
        Assert.assertEquals("TRACE log", outputStreamCaptor.toString().substring(20),
                TITLE + "[TRACE] foo" + LS);
        clear();
        DongTaiLog.trace("foo {} {}", "bar", "baz");
        Assert.assertEquals("TRACE log format", outputStreamCaptor.toString().substring(20),
                TITLE + "[TRACE] foo bar baz" + LS);
        clear();
        DongTaiLog.trace("foo", new Exception("bar"));
        Assert.assertEquals("TRACE log message with exception", outputStreamCaptor.toString().substring(20),
                TITLE + "[TRACE] foo, Exception: java.lang.Exception: bar" + LS);

        DongTaiLog.setLevel(DongTaiLog.LogLevel.DEBUG);
        clear();
        DongTaiLog.debug("foo");
        Assert.assertEquals("DEBUG log", outputStreamCaptor.toString().substring(20),
                TITLE + "[DEBUG] foo" + LS);
        clear();
        DongTaiLog.debug("foo {} {}", "bar", "baz");
        Assert.assertEquals("DEBUG log format", outputStreamCaptor.toString().substring(20),
                TITLE + "[DEBUG] foo bar baz" + LS);
        clear();
        DongTaiLog.debug("foo", new Exception("bar"));
        Assert.assertEquals("DEBUG log message with exception", outputStreamCaptor.toString().substring(20),
                TITLE + "[DEBUG] foo, Exception: java.lang.Exception: bar" + LS);

        DongTaiLog.setLevel(DongTaiLog.LogLevel.INFO);
        clear();
        DongTaiLog.info("foo");
        Assert.assertEquals("INFO log", outputStreamCaptor.toString().substring(20),
                TITLE + "[INFO] foo" + LS);
        clear();
        DongTaiLog.info("foo {} {}", "bar", "baz");
        Assert.assertEquals("INFO log format", outputStreamCaptor.toString().substring(20),
                TITLE + "[INFO] foo bar baz" + LS);
        clear();
        DongTaiLog.info("foo", new Exception("bar"));
        Assert.assertEquals("INFO log message with exception", outputStreamCaptor.toString().substring(20),
                TITLE + "[INFO] foo, Exception: java.lang.Exception: bar" + LS);

        DongTaiLog.setLevel(DongTaiLog.LogLevel.WARN);
        clear();
        DongTaiLog.warn("foo");
        Assert.assertEquals("WARN log", outputStreamCaptor.toString().substring(20),
                TITLE + "[WARN] foo" + LS);
        clear();
        DongTaiLog.warn("foo {} {}", "bar", "baz");
        Assert.assertEquals("WARN log format", outputStreamCaptor.toString().substring(20),
                TITLE + "[WARN] foo bar baz" + LS);
        clear();
        DongTaiLog.warn("foo", new Exception("bar"));
        Assert.assertEquals("WARN log message with exception", outputStreamCaptor.toString().substring(20),
                TITLE + "[WARN] foo, Exception: java.lang.Exception: bar" + LS);

        DongTaiLog.setLevel(DongTaiLog.LogLevel.ERROR);
        clear();
        DongTaiLog.error("foo");
        Assert.assertEquals("ERROR log", outputStreamCaptor.toString().substring(20),
                TITLE + "[ERROR] foo" + LS);
        clear();
        DongTaiLog.error("foo {} {}", "bar", "baz");
        Assert.assertEquals("ERROR log format", outputStreamCaptor.toString().substring(20),
                TITLE + "[ERROR] foo bar baz" + LS);
        clear();
        DongTaiLog.error("foo", new Exception("bar"));
        Assert.assertEquals("ERROR log message with exception", outputStreamCaptor.toString().substring(20),
                TITLE + "[ERROR] foo, Exception: java.lang.Exception: bar" + LS);
        clear();
        DongTaiLog.error(new Exception("bar"));
        Assert.assertEquals("ERROR log exception", outputStreamCaptor.toString().substring(20),
                TITLE + "[ERROR] Exception: java.lang.Exception: bar" + LS);
        clear();
        DongTaiLog.error(110, "foo {} {}", "bar", "baz");
        Assert.assertEquals("ERROR log format", outputStreamCaptor.toString().substring(20),
                TITLE + "[ERROR] [110] foo bar baz" + LS);
        clear();
        DongTaiLog.error(110, "foo {} {}", "bar", "baz", new Exception("bar"));
        Assert.assertEquals("ERROR log format with code and exception", outputStreamCaptor.toString().substring(20),
                TITLE + "[ERROR] [110] foo bar baz, Exception: java.lang.Exception: bar" + LS);

        clear();
        DongTaiLog.error(110, "foo {}", "bar", "baz", new Exception("bar"));
        Assert.assertEquals("ERROR log format less with code and exception", outputStreamCaptor.toString().substring(20),
                TITLE + "[ERROR] [110] foo bar, Exception: java.lang.Exception: bar" + LS);
        clear();
        DongTaiLog.error(110, "foo {} {} {}", "bar", "baz", new Exception("bar"));
        Assert.assertEquals("ERROR log format more with code and exception", outputStreamCaptor.toString().substring(20),
                TITLE + "[ERROR] [110] foo bar baz {}, Exception: java.lang.Exception: bar" + LS);

        clear();
        DongTaiLog.error(ErrorCode.AGENT_PREMAIN_INVOKE_FAILED, new Exception("bar"));
        Assert.assertEquals("ERROR log with ErrorCode and exception", outputStreamCaptor.toString().substring(20),
                TITLE + "[ERROR] [10101] agent premain invoke failed, Exception: java.lang.Exception: bar" + LS);
    }
}
