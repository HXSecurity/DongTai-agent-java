package io.dongtai.log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;

/**
 * @author niuerzhuang@huoxian.cn
 */
public class DongTaiLog {
    public static boolean ENABLED;
    private static String LOG_DIR;
    private static String LOG_PATH;
    public static boolean ENABLE_COLOR;
    public static LogLevel LEVEL = getCurrentLevel();

    private static final String RESET = "\033[0m";
    private static final int RED = 31;
    private static final int GREEN = 32;
    private static final int YELLOW = 33;
    private static final int BLUE = 34;

    // 5min
    public static int FREQUENT_INTERVAL = 300000;

    private static final String TITLE = "[io.dongtai.iast.agent] ";
    private static final String TITLE_COLOR = "[" + colorStr("io.dongtai.iast.agent", BLUE) + "] ";

    private static final Set<ErrorCode> RESTRICTED_ERRORS = new HashSet<ErrorCode>(Arrays.asList(
            ErrorCode.AGENT_MONITOR_COLLECT_PERFORMANCE_METRICS_FAILED,
            ErrorCode.AGENT_MONITOR_CHECK_PERFORMANCE_METRICS_FAILED,
            ErrorCode.AGENT_MONITOR_GET_DISK_USAGE_FAILED,
            ErrorCode.REPORT_SEND_FAILED,
            ErrorCode.REPLAY_REQUEST_FAILED,
            ErrorCode.GRAPH_BUILD_AND_REPORT_FAILED,
            ErrorCode.TAINT_COMMAND_GET_PARAMETERS_FAILED,
            ErrorCode.TAINT_COMMAND_RANGE_PROCESS_FAILED
    ));

    private static final ConcurrentHashMap<ErrorCode, ErrorRecord> ERROR_RECORD_MAP = new ConcurrentHashMap<ErrorCode, ErrorRecord>();

    static {
        if (System.console() != null && !System.getProperty("os.name").toLowerCase().contains("windows")) {
            ENABLE_COLOR = true;
        }

        ENABLED = IastProperties.isEnabled();
        LOG_DIR = IastProperties.getLogDir();
    }

    private static class ErrorRecord {
        private long lastWriteTime;
        private int count;

        public ErrorRecord() {
            this.lastWriteTime = new Date().getTime();
            this.count = 0;
        }

        public boolean needWrite() {
            long now = new Date().getTime();
            // 5min
            return now - this.lastWriteTime > FREQUENT_INTERVAL;
        }

        public int getCount() {
            return this.count;
        }

        public void incrementCount() {
            this.count++;
        }

        public void rotate() {
            this.lastWriteTime = new Date().getTime();
            this.count = 0;
        }
    }

    public static void configure(Integer agentId) throws Exception {
        ENABLED = IastProperties.isEnabled();
        if (!ENABLED) {
            return;
        }
        setLevel(getCurrentLevel());

        LOG_DIR = IastProperties.getLogDir();
        if (LOG_DIR.isEmpty()) {
            return;
        }

        if (agentId == null || agentId < 0) {
            agentId = 0;
        }

        try {
            File f = new File(LOG_DIR);
            if (!f.exists()) {
                f.mkdirs();
            }
        } catch (Throwable e) {
            throw new Exception("init log dir " + LOG_DIR + " failed: " + e.getMessage());
        }

        String path = LOG_DIR + File.separator + "dongtai_javaagent-" + String.valueOf(agentId) + ".log";
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            LOG_PATH = path;
        } catch (Throwable e) {
            throw new Exception(TITLE + "init log file " + LOG_PATH + " failed: " + e.getMessage());
        }
    }

    public static String getLogPath() {
        return LOG_PATH;
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
        return parseLevel(logLevel);
    }

    public static LogLevel parseLevel(String logLevel) {
        LogLevel lvl;
        if ("trace".equalsIgnoreCase(logLevel)) {
            lvl = LogLevel.TRACE;
        } else if ("info".equalsIgnoreCase(logLevel)) {
            lvl = LogLevel.INFO;
        } else if ("debug".equalsIgnoreCase(logLevel)) {
            lvl = LogLevel.DEBUG;
        } else if ("warn".equalsIgnoreCase(logLevel)) {
            lvl = LogLevel.WARN;
        } else if ("error".equalsIgnoreCase(logLevel)) {
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
        return ENABLED && lvl.getLevel().intValue() >= LEVEL.getLevel().intValue();
    }

    private static String colorStr(String msg, int colorCode) {
        return "\033[" + colorCode + "m" + msg + RESET;
    }

    private static String getPrefix(LogLevel lvl, int code, int cnt, boolean useColor) {
        String prefix;
        if (useColor) {
            prefix = getTime() + TITLE_COLOR + lvl.getColorPrefix();
        } else {
            prefix = getTime() + TITLE + lvl.getPrefix();
        }

        if (code > 0) {
            prefix += "[" + String.valueOf(code) + "] ";
        }

        if (cnt > 0) {
            prefix += "[occurred " + String.valueOf(cnt) + " times] ";
        }

        return prefix;
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

    private static void log(LogLevel lvl, ErrorCode ec, String fmt, Object... arguments) {
        if (!canLog(lvl)) {
            return;
        }

        int cnt = 0;
        if (RESTRICTED_ERRORS.contains(ec)) {
            ErrorRecord er = ERROR_RECORD_MAP.get(ec);
            if (er == null) {
                ERROR_RECORD_MAP.put(ec, new ErrorRecord());
            } else {
                if (!er.needWrite()) {
                    er.incrementCount();
                    return;
                }

                cnt = er.getCount();
                er.rotate();
            }
        }

        int code = ec.getCode();
        Throwable t = null;
        String msg = fmt;
        if (arguments.length == 1 && arguments[0] instanceof Throwable) {
            t = (Throwable) arguments[0];
        } else if (arguments.length > 0) {
            if (arguments[arguments.length - 1] instanceof Throwable) {
                t = (Throwable) arguments[arguments.length - 1];
                Object[] newArguments = new Object[arguments.length - 1];
                System.arraycopy(arguments, 0, newArguments, 0, arguments.length - 1);
                msg = format(fmt, newArguments);
            } else {
                msg = format(fmt, arguments);
            }
        }

        msg = getMessage(msg, t);
        if (msg.isEmpty()) {
            return;
        }
        System.out.println(getPrefix(lvl, code, cnt, ENABLE_COLOR) + msg);
        writeLogToFile(getPrefix(lvl, code, cnt, false) + msg, t);
    }

    public static void trace(String fmt, Object... arguments) {
        log(LogLevel.TRACE, ErrorCode.NO_CODE, fmt, arguments);
    }

    public static void debug(String fmt, Object... arguments) {
        log(LogLevel.DEBUG, ErrorCode.NO_CODE, fmt, arguments);
    }

    public static void info(String fmt, Object... arguments) {
        log(LogLevel.INFO, ErrorCode.NO_CODE, fmt, arguments);
    }

    public static void warn(ErrorCode ec, Object... arguments) {
        log(LogLevel.WARN, ec, ec.getMessage(), arguments);
    }

    public static void warn(String format, Object... arguments) {
        log(LogLevel.WARN, ErrorCode.NO_CODE, format, arguments);
    }

    public static void error(ErrorCode ec, Object... arguments) {
        log(LogLevel.ERROR, ec, ec.getMessage(), arguments);
    }

    public static void error(String format, Object... arguments) {
        log(LogLevel.ERROR, ErrorCode.NO_CODE, format, arguments);
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
        if (LOG_PATH == null || LOG_PATH.isEmpty()) {
            return;
        }
        FileOutputStream o = null;
        try {
            if (t != null) {
                StringWriter stringWriter = new StringWriter();
                t.printStackTrace(new PrintWriter(stringWriter));
                msg = msg + ", StackTrace: " + stringWriter;
            }

            File file = new File(LOG_PATH);
            o = new FileOutputStream(file, true);
            o.write(msg.getBytes());
            o.write(System.getProperty("line.separator").getBytes());
            o.flush();
            o.close();
        } catch (Throwable e) {
            if (o != null) {
                try {
                    o.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }
}
