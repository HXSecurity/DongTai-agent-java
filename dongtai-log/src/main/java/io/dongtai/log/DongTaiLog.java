package io.dongtai.log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.regex.Matcher;

/**
 * @author niuerzhuang@huoxian.cn
 */
public class DongTaiLog {
    public static boolean enablePrintLog;
    private static String logDir;
    private static String logPath = "";
    public static boolean enableColor;
    public static LogLevel LEVEL = getCurrentLevel();

    private static final String RESET = "\033[0m";
    private static final int RED = 31;
    private static final int GREEN = 32;
    private static final int YELLOW = 33;
    private static final int BLUE = 34;

    private static final String TITLE = "[io.dongtai.iast.agent] ";
    private static final String TITLE_COLOR = "[" + colorStr("io.dongtai.iast.agent", BLUE) + "] ";

    static {
        if (System.console() != null && !System.getProperty("os.name").toLowerCase().contains("windows")) {
            enableColor = true;
        }

        enablePrintLog = !"false".equalsIgnoreCase(IastProperties.enablePrintLog());
        logDir = IastProperties.getLogDir();
    }

    public static void initialize(int id) throws Exception {
        enablePrintLog = !"false".equalsIgnoreCase(IastProperties.enablePrintLog());
        logDir = IastProperties.getLogDir();
        if (!enablePrintLog || logDir.isEmpty()) {
            return;
        }

        try {
            File f = new File(logDir);
            if (!f.exists()) {
                f.mkdirs();
            }
        } catch (Throwable e) {
            throw new Exception("init log dir " + logDir + " failed: " + e.getMessage());
        }

        String path = logDir + File.separator + "dongtai_javaagent-" + String.valueOf(id) + ".log";
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            logPath = path;
        } catch (Throwable e) {
            System.out.println(TITLE + "init log file " + logPath + "failed: " + e.getMessage());
        }

        setLevel(getCurrentLevel());
    }

    public static String getLogPath() {
        return logPath;
    }

    public enum LogLevel {
        TRACE(Level.FINEST, "[TRACE] ", "[" + colorStr("TRACE", BLUE) + "] "),
        DEBUG(Level.FINER, "[DEBUG] ", "[" + colorStr("DEBUG", BLUE) + "] "),
        INFO(Level.INFO, "[INFO] ", "[" + colorStr("INFO", GREEN) + "] "),
        WARN(Level.WARNING, "[WARN] ", "[" + colorStr("WARN", YELLOW) + "] "),
        ERROR(Level.SEVERE, "[ERROR] ", "[" + colorStr("ERROR", RED) + "] "),
        ;

        private final Level level;
        private final String prefix;
        private final String colorPrefix;

        LogLevel(Level level, String prefix, String colorPrefix) {
            this.level = level;
            this.prefix = prefix;
            this.colorPrefix = colorPrefix;
        }

        private Level getLevel() {
            return level;
        }

        private String getPrefix() {
            return prefix;
        }

        private String getColorPrefix() {
            return colorPrefix;
        }
    }

    private static LogLevel getCurrentLevel() {
        String logLevel = IastProperties.getLogLevel();
        LogLevel lvl;
        if ("trace".equals(logLevel)) {
            lvl = LogLevel.TRACE;
        } else if ("info".equals(logLevel)) {
            lvl = LogLevel.INFO;
        } else if ("debug".equals(logLevel)) {
            lvl = LogLevel.DEBUG;
        } else if ("warn".equals(logLevel)) {
            lvl = LogLevel.WARN;
        } else if ("error".equals(logLevel)) {
            lvl = LogLevel.ERROR;
        } else {
            lvl = LogLevel.INFO;
        }
        return lvl;
    }

    public static void setLevel(LogLevel lvl) {
        LEVEL = lvl;
    }

    public static boolean canLog(LogLevel lvl) {
        return enablePrintLog && lvl.getLevel().intValue() >= LEVEL.getLevel().intValue();
    }

    private static String colorStr(String msg, int colorCode) {
        return "\033[" + colorCode + "m" + msg + RESET;
    }

    private static String getPrefix(LogLevel lvl, boolean useColor) {
        if (useColor) {
            return getTime() + TITLE_COLOR + lvl.getColorPrefix();
        }
        return getTime() + TITLE + lvl.getPrefix();
    }

    private static String getMessage(String msg, Throwable t) {
        if (t != null) {
            if (msg == null || msg.isEmpty()) {
                msg = "Exception: " + t.toString();
            } else {
                msg += ", Exception: " + t.toString();
            }
        }
        return msg;
    }

    public static void log(LogLevel lvl, String msg, Throwable t) {
        if (!canLog(lvl)) {
            return;
        }
        msg = getMessage(msg, t);
        if (msg.isEmpty()) {
            return;
        }
        System.out.println(getPrefix(lvl, enableColor) + msg);
        writeLogToFile(getPrefix(lvl, false) + msg, t);
    }

    public static void trace(String msg, Throwable t) {
        log(LogLevel.TRACE, msg, t);
    }

    public static void trace(String msg) {
        trace(msg, (Throwable) null);
    }

    public static void trace(String format, Object... arguments) {
        if (canLog(LogLevel.TRACE)) {
            trace(format(format, arguments));
        }
    }

    public static void trace(Throwable t) {
        trace("", t);
    }

    public static void debug(String msg, Throwable t) {
        log(LogLevel.DEBUG, msg, t);
    }

    public static void debug(String msg) {
        debug(msg, (Throwable) null);
    }

    public static void debug(String format, Object... arguments) {
        if (canLog(LogLevel.DEBUG)) {
            debug(format(format, arguments));
        }
    }

    public static void debug(Throwable t) {
        debug("", t);
    }

    public static void info(String msg, Throwable t) {
        log(LogLevel.INFO, msg, t);
    }

    public static void info(String msg) {
        info(msg, (Throwable) null);
    }

    public static void info(String format, Object... arguments) {
        if (canLog(LogLevel.INFO)) {
            info(format(format, arguments));
        }
    }

    public static void info(Throwable t) {
        info("", t);
    }

    public static void warn(String msg, Throwable t) {
        log(LogLevel.WARN, msg, t);
    }

    public static void warn(String msg) {
        warn(msg, (Throwable) null);
    }

    public static void warn(String format, Object... arguments) {
        if (canLog(LogLevel.WARN)) {
            warn(format(format, arguments));
        }
    }

    public static void warn(Throwable t) {
        warn("", t);
    }

    public static void error(String msg, Throwable t) {
        log(LogLevel.ERROR, msg, t);
    }

    public static void error(String msg) {
        error(msg, (Throwable) null);
    }

    public static void error(String format, Object... arguments) {
        if (canLog(LogLevel.ERROR)) {
            error(format(format, arguments));
        }
    }

    public static void error(Throwable t) {
        error("", t);
    }

    private static String format(String from, Object... arguments) {
        if (from != null) {
            String computed = from;
            if (arguments != null && arguments.length != 0) {
                for (Object argument : arguments) {
                    computed = computed.replaceFirst("\\{\\}", argument == null ? "NULL" : Matcher.quoteReplacement(argument.toString()));
                }
            }
            return computed;
        }
        return null;
    }

    private static String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return simpleDateFormat.format(new Date()) + " ";
    }

    private static void writeLogToFile(String msg, Throwable t) {
        if (logPath.isEmpty()) {
            return;
        }
        FileOutputStream o = null;
        File file = new File(logPath);
        try {
            if (t != null) {
                StringWriter stringWriter = new StringWriter();
                t.printStackTrace(new PrintWriter(stringWriter));
                msg = msg + ", StackTrace: " + stringWriter;
            }

            o = new FileOutputStream(file, true);
            o.write(msg.getBytes());
            o.write(System.getProperty("line.separator").getBytes());
            o.flush();
            o.close();
        } catch (Throwable e) {
            System.out.println(TITLE + "the log file " + file.getPath() + " is not writable: " + e.toString());
        }
    }
}
