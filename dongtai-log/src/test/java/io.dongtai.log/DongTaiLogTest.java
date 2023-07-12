package io.dongtai.log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertEquals("DEBUG level ignore", "", outputStreamCaptor.toString());

        DongTaiLog.setLevel(DongTaiLog.LogLevel.INFO);
        clear();
        DongTaiLog.trace("foo");
        Assert.assertEquals("INFO level ignore", "", outputStreamCaptor.toString());
        clear();
        DongTaiLog.debug("foo");
        Assert.assertEquals("INFO level ignore", "", outputStreamCaptor.toString());

        DongTaiLog.setLevel(DongTaiLog.LogLevel.WARN);
        clear();
        DongTaiLog.trace("foo");
        Assert.assertEquals("WARN level ignore", "", outputStreamCaptor.toString());
        clear();
        DongTaiLog.debug("foo");
        Assert.assertEquals("WARN level ignore", "", outputStreamCaptor.toString());
        clear();
        DongTaiLog.info("foo");
        Assert.assertEquals("WARN level ignore", "", outputStreamCaptor.toString());

        DongTaiLog.setLevel(DongTaiLog.LogLevel.ERROR);
        clear();
        DongTaiLog.trace("foo");
        Assert.assertEquals("ERROR level ignore", "", outputStreamCaptor.toString());
        clear();
        DongTaiLog.debug("foo");
        Assert.assertEquals("ERROR level ignore", "", outputStreamCaptor.toString());
        clear();
        DongTaiLog.info("foo");
        Assert.assertEquals("ERROR level ignore", "", outputStreamCaptor.toString());
        clear();
        DongTaiLog.warn("foo");
        Assert.assertEquals("ERROR level ignore", "", outputStreamCaptor.toString());

        DongTaiLog.setLevel(DongTaiLog.LogLevel.TRACE);
        clear();
        DongTaiLog.trace("foo");
        Assert.assertEquals("TRACE log", TITLE + "[TRACE] foo" + LS,
                outputStreamCaptor.toString().substring(20));
        clear();
        DongTaiLog.trace("foo {} {}", "bar", "baz");
        Assert.assertEquals("TRACE log format", TITLE + "[TRACE] foo bar baz" + LS,
                outputStreamCaptor.toString().substring(20));
        clear();
        DongTaiLog.trace("foo", new Exception("bar"));
        Assert.assertEquals("TRACE log message with exception",
                TITLE + "[TRACE] foo, Exception: java.lang.Exception: bar" + LS,
                outputStreamCaptor.toString().substring(20));

        DongTaiLog.setLevel(DongTaiLog.LogLevel.DEBUG);
        clear();
        DongTaiLog.debug("foo");
        Assert.assertEquals("DEBUG log", TITLE + "[DEBUG] foo" + LS,
                outputStreamCaptor.toString().substring(20));
        clear();
        DongTaiLog.debug("foo {} {}", "bar", "baz");
        Assert.assertEquals("DEBUG log format", TITLE + "[DEBUG] foo bar baz" + LS,
                outputStreamCaptor.toString().substring(20));
        clear();
        DongTaiLog.debug("foo", new Exception("bar"));
        Assert.assertEquals("DEBUG log message with exception",
                TITLE + "[DEBUG] foo, Exception: java.lang.Exception: bar" + LS,
                outputStreamCaptor.toString().substring(20));

        DongTaiLog.setLevel(DongTaiLog.LogLevel.INFO);
        clear();
        DongTaiLog.info("foo");
        Assert.assertEquals("INFO log", TITLE + "[INFO] foo" + LS,
                outputStreamCaptor.toString().substring(20));
        clear();
        DongTaiLog.info("foo {} {}", "bar", "baz");
        Assert.assertEquals("INFO log format", TITLE + "[INFO] foo bar baz" + LS,
                outputStreamCaptor.toString().substring(20));
        clear();
        DongTaiLog.info("foo", new Exception("bar"));
        Assert.assertEquals("INFO log message with exception",
                TITLE + "[INFO] foo, Exception: java.lang.Exception: bar" + LS,
                outputStreamCaptor.toString().substring(20));

        DongTaiLog.setLevel(DongTaiLog.LogLevel.WARN);
        clear();
        DongTaiLog.warn("foo");
        Assert.assertEquals("WARN log", TITLE + "[WARN] foo" + LS,
                outputStreamCaptor.toString().substring(20));
        clear();
        DongTaiLog.warn("foo {} {}", "bar", "baz");
        Assert.assertEquals("WARN log format", TITLE + "[WARN] foo bar baz" + LS,
                outputStreamCaptor.toString().substring(20));
        clear();
        DongTaiLog.warn("foo", new Exception("bar"));
        Assert.assertEquals("WARN log message with exception",
                TITLE + "[WARN] foo, Exception: java.lang.Exception: bar" + LS,
                outputStreamCaptor.toString().substring(20));

        DongTaiLog.setLevel(DongTaiLog.LogLevel.ERROR);
        clear();
        DongTaiLog.error("foo");
        Assert.assertEquals("ERROR log", TITLE + "[ERROR] foo" + LS,
                outputStreamCaptor.toString().substring(20));
        clear();
        DongTaiLog.error("foo {} {}", "bar", "baz");
        Assert.assertEquals("ERROR log format", TITLE + "[ERROR] foo bar baz" + LS,
                outputStreamCaptor.toString().substring(20));
        clear();
        DongTaiLog.error("foo", new Exception("bar"));
        Assert.assertEquals("ERROR log message with exception",
                TITLE + "[ERROR] foo, Exception: java.lang.Exception: bar" + LS,
                outputStreamCaptor.toString().substring(20));

        int code;
        String fmt;
        clear();
        DongTaiLog.error(ErrorCode.AGENT_PREMAIN_INVOKE_FAILED, new Exception("bar"));
        code = ErrorCode.AGENT_PREMAIN_INVOKE_FAILED.getCode();
        fmt = ErrorCode.AGENT_PREMAIN_INVOKE_FAILED.getMessage();
        Assert.assertEquals("ERROR log with ErrorCode and exception",
                TITLE + "[ERROR] [" + code + "] " + fmt + ", Exception: java.lang.Exception: bar" + LS,
                outputStreamCaptor.toString().substring(20));
        clear();
        DongTaiLog.error(ErrorCode.get("AGENT_PREMAIN_INVOKE_FAILED"), new Exception("bar"));
        Assert.assertEquals("ERROR log with ErrorCode name and exception",
                TITLE + "[ERROR] [" + code + "] " + fmt + ", Exception: java.lang.Exception: bar" + LS,
                outputStreamCaptor.toString().substring(20));
        clear();
        DongTaiLog.error(ErrorCode.JATTACH_EXTRACT_FAILED, "/tmp/test");
        code = ErrorCode.JATTACH_EXTRACT_FAILED.getCode();
        fmt = String.format(ErrorCode.JATTACH_EXTRACT_FAILED.getMessage().replaceAll("\\{\\}", "%s"), "/tmp/test");
        Assert.assertEquals("ERROR log with ErrorCode and arguments",
                TITLE + "[ERROR] [" + code + "] " + fmt + LS,
                outputStreamCaptor.toString().substring(20));
        clear();
        DongTaiLog.error(ErrorCode.get("JATTACH_EXTRACT_FAILED"), "/tmp/test");
        Assert.assertEquals("ERROR log with ErrorCode name and arguments",
                TITLE + "[ERROR] [" + code + "] " + fmt + LS,
                outputStreamCaptor.toString().substring(20));
        clear();
        DongTaiLog.error(ErrorCode.get("NOT EXISTS"));
        code = ErrorCode.UNKNOWN.getCode();
        Assert.assertEquals("ERROR log with ErrorCode invalid name",
                TITLE + "[ERROR] [" + code + "] NOT EXISTS" + LS,
                outputStreamCaptor.toString().substring(20));
        clear();
        DongTaiLog.error(ErrorCode.get("NOT EXISTS"), "/tmp/test", "foo");
        Assert.assertEquals("ERROR log with ErrorCode invalid name and arguments",
                TITLE + "[ERROR] [" + code + "] NOT EXISTS" + LS,
                outputStreamCaptor.toString().substring(20));

        // System.setOut(standardOut);
        int fi = DongTaiLog.FREQUENT_INTERVAL;
        DongTaiLog.FREQUENT_INTERVAL = 3000;
        code = ErrorCode.REPORT_SEND_FAILED.getCode();
        fmt = String.format(ErrorCode.REPORT_SEND_FAILED.getMessage().replaceAll("\\{\\}", "%s"), "a", "b");
        for (int i = 0; i < 8; i++) {
            clear();
            DongTaiLog.error(ErrorCode.REPORT_SEND_FAILED, "a", "b");
            if (i == 0) {
                String msg = outputStreamCaptor.toString();
                Assert.assertTrue("ERROR log with frequent log " + i, msg.length() > 20);
                Assert.assertEquals("ERROR log with frequent log " + i,
                        TITLE + "[ERROR] [" + code + "] " + fmt + LS,
                        msg.substring(20));
            } else if (i % 3 == 0) {
                String msg = outputStreamCaptor.toString();
                Assert.assertTrue("ERROR log with frequent log " + i, msg.length() > 20);
                Assert.assertEquals("ERROR log with frequent log " + i,
                        TITLE + "[ERROR] [" + code + "] [occurred 2 times] " + fmt + LS,
                        outputStreamCaptor.toString().substring(20));
            } else {
                Assert.assertEquals("ERROR log with frequent log " + i,
                        "", outputStreamCaptor.toString());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
        }
        DongTaiLog.FREQUENT_INTERVAL = fi;

        clear();
    }

    @Test
    public void formatTest() {

        Assert.assertEquals("this is foo, i am ok", DongTaiLog.format("this is {}, i am ok", "foo"));
        Assert.assertEquals("this is {} foo {}, i am ok", DongTaiLog.format("this is {}, i am ok", "{} foo {}"));

        Assert.assertEquals("foo", DongTaiLog.format("{}", "foo"));
        Assert.assertEquals("foo begin", DongTaiLog.format("{} begin", "foo"));
        Assert.assertEquals("end foo", DongTaiLog.format("end {}", "foo"));

        Assert.assertEquals("foobar", DongTaiLog.format("{}{}", "foo", "bar"));
        Assert.assertEquals("foo123bar", DongTaiLog.format("{}{}{}", "foo", "123", "bar"));
        Assert.assertEquals("foo 123 bar", DongTaiLog.format("{} {} {}", "foo", "123", "bar"));

        Assert.assertEquals("{}", DongTaiLog.format("\\{}", "foo"));
        Assert.assertEquals("{}", DongTaiLog.format("{\\}", "foo"));

        Assert.assertEquals("{", DongTaiLog.format("{", "foo"));
        Assert.assertEquals("foo {", DongTaiLog.format("foo {", "foo"));
        Assert.assertEquals("{ foo", DongTaiLog.format("{ foo", "foo"));
        Assert.assertEquals("}", DongTaiLog.format("}", "foo"));
        Assert.assertEquals("}{", DongTaiLog.format("}{", "foo"));
        Assert.assertEquals("}{", DongTaiLog.format("}{", "foo"));

        // 参数不够
        Assert.assertEquals("foo {} {}", DongTaiLog.format("{} {} {}", "foo"));
        // 参数过多
        Assert.assertEquals("foo", DongTaiLog.format("{}", "foo", "bar"));

    }

}
