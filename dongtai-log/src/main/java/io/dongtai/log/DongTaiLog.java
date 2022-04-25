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

    static boolean enablePrintLog;
    static String filePath;
    static boolean enableColor;
    static boolean isCreateLog = false;
    public static java.util.logging.Level LEVEL;

    private static final String RESET = "\033[0m";
    private static final int RED = 31;
    private static final int GREEN = 32;
    private static final int YELLOW = 33;
    private static final int BLUE = 34;

    private static final String TITTLE = "[io.dongtai.iast.agent] ";
    private static final String TITTLE_COLOR_PREFIX = "[" + colorStr("io.dongtai.iast.agent", BLUE) + "] ";

    private static final String TRACE_PREFIX = "[TRACE] ";
    private static final String TRACE_COLOR_PREFIX = "[" + colorStr("TRACE", GREEN) + "] ";

    private static final String DEBUG_PREFIX = "[DEBUG] ";
    private static final String DEBUG_COLOR_PREFIX = "[" + colorStr("DEBUG", GREEN) + "] ";

    private static final String INFO_PREFIX = "[INFO] ";
    private static final String INFO_COLOR_PREFIX = "[" + colorStr("INFO", GREEN) + "] ";

    private static final String WARN_PREFIX = "[WARN] ";
    private static final String WARN_COLOR_PREFIX = "[" + colorStr("WARN", YELLOW) + "] ";

    private static final String ERROR_PREFIX = "[ERROR] ";
    private static final String ERROR_COLOR_PREFIX = "[" + colorStr("ERROR", RED) + "] ";

    static {
        if (System.console() != null && !System.getProperty("os.name").toLowerCase().contains("windows")) {
            enableColor = true;
        }
        String logLevel = IastProperties.getLogLevel();
        if ("info".equals(logLevel)) {
            LEVEL = Level.CONFIG;
        } else if ("debug".equals(logLevel)) {
            LEVEL = Level.FINER;
        } else if ("warn".equals(logLevel)) {
            LEVEL = Level.WARNING;
        } else if ("error".equals(logLevel)) {
            LEVEL = Level.SEVERE;
        } else if ("trace".equals(logLevel)) {
            LEVEL = Level.FINEST;
        }
    }

    /**
     * set logger Level
     *
     * @param level
     * @return
     * @see java.util.logging.Level
     */
    public static Level level(Level level) {
        Level old = LEVEL;
        LEVEL = level;
        return old;
    }

    private static String colorStr(String msg, int colorCode) {
        return "\033[" + colorCode + "m" + msg + RESET;
    }

    public static void trace(String msg) {
        if (canLog(Level.FINEST) && enablePrintLog) {
            if (enableColor) {
                System.out.println(getTime() + TITTLE_COLOR_PREFIX + TRACE_COLOR_PREFIX + msg);
            } else {
                System.out.println(getTime() + TITTLE + TRACE_PREFIX + msg);
            }
            msg = getTime() + TITTLE + TRACE_PREFIX + msg;
            writeLogToFile(msg);
        }
    }

    public static void trace(String format, Object... arguments) {
        if (canLog(Level.FINEST)) {
            trace(format(format, arguments));
        }
    }

    public static void trace(Throwable t) {
        if (canLog(Level.FINEST) && enablePrintLog) {
            String msg = t.getMessage();
            if (enableColor) {
                System.out.println(getTime() + TITTLE_COLOR_PREFIX + TRACE_COLOR_PREFIX + msg);
            } else {
                System.out.println(getTime() + TITTLE + TRACE_PREFIX + msg);
            }
            msg = getTime() + TITTLE + TRACE_PREFIX + msg;
            StringWriter stringWriter = new StringWriter();
            t.printStackTrace(new PrintWriter(stringWriter));
            msg = msg + stringWriter;
            writeLogToFile(msg);
        }
    }

    public static void debug(String msg) {
        if (canLog(Level.FINER) && enablePrintLog) {
            if (enableColor) {
                System.out.println(getTime() + TITTLE_COLOR_PREFIX + DEBUG_COLOR_PREFIX + msg);
            } else {
                System.out.println(getTime() + TITTLE + DEBUG_PREFIX + msg);
            }
            msg = getTime() + TITTLE + DEBUG_PREFIX + msg;
            writeLogToFile(msg);
        }
    }

    public static void debug(String format, Object... arguments) {
        if (canLog(Level.FINER)) {
            debug(format(format, arguments));
        }
    }

    public static void debug(Throwable t) {
        if (canLog(Level.FINER) && enablePrintLog) {
            String msg = t.getMessage();
            if (enableColor) {
                System.out.println(getTime() + TITTLE_COLOR_PREFIX + DEBUG_COLOR_PREFIX + msg);
            } else {
                System.out.println(getTime() + TITTLE + DEBUG_PREFIX + msg);
            }
            msg = getTime() + TITTLE + DEBUG_PREFIX + msg;
            StringWriter stringWriter = new StringWriter();
            t.printStackTrace(new PrintWriter(stringWriter));
            msg = msg + stringWriter;
            writeLogToFile(msg);
        }
    }

    public static void info(String msg) {
        if (canLog(Level.CONFIG) && enablePrintLog) {
            if (enableColor) {
                System.out.println(getTime() + TITTLE_COLOR_PREFIX + INFO_COLOR_PREFIX + msg);
            } else {
                System.out.println(getTime() + TITTLE + INFO_PREFIX + msg);
            }
            msg = getTime() + TITTLE + INFO_PREFIX + msg;
            writeLogToFile(msg);
        }
    }

    public static void info(String format, Object... arguments) {
        if (canLog(Level.CONFIG)) {
            info(format(format, arguments));
        }
    }

    public static void info(Throwable t) {
        if (canLog(Level.CONFIG) && enablePrintLog) {
            String msg = t.getMessage();
            if (enableColor) {
                System.out.println(getTime() + TITTLE_COLOR_PREFIX + INFO_COLOR_PREFIX + msg);
            } else {
                System.out.println(getTime() + TITTLE + INFO_PREFIX + msg);
            }
            msg = getTime() + TITTLE + INFO_PREFIX + msg;
            StringWriter stringWriter = new StringWriter();
            t.printStackTrace(new PrintWriter(stringWriter));
            msg = msg + stringWriter;
            writeLogToFile(msg);
        }
    }

    public static void warn(String msg) {
        if (canLog(Level.WARNING) && enablePrintLog) {
            if (enableColor) {
                System.out.println(getTime() + TITTLE_COLOR_PREFIX + WARN_COLOR_PREFIX + msg);
            } else {
                System.out.println(getTime() + TITTLE + WARN_PREFIX + msg);
            }
            msg = getTime() + TITTLE + WARN_PREFIX + msg;
            writeLogToFile(msg);
        }
    }

    public static void warn(String format, Object... arguments) {
        if (canLog(Level.WARNING)) {
            warn(format(format, arguments));
        }
    }

    public static void warn(Throwable t) {
        if (canLog(Level.WARNING) && enablePrintLog) {
            String msg = t.getMessage();
            if (enableColor) {
                System.out.println(getTime() + TITTLE_COLOR_PREFIX + WARN_COLOR_PREFIX + msg);
            } else {
                System.out.println(getTime() + TITTLE + WARN_PREFIX + msg);
            }
            msg = getTime() + TITTLE + WARN_PREFIX + msg;
            StringWriter stringWriter = new StringWriter();
            t.printStackTrace(new PrintWriter(stringWriter));
            msg = msg + stringWriter;
            writeLogToFile(msg);
        }
    }

    public static void error(String msg) {
        if (canLog(Level.SEVERE) && enablePrintLog) {
            if (enableColor) {
                System.out.println(getTime() + TITTLE_COLOR_PREFIX + ERROR_COLOR_PREFIX + msg);
            } else {
                System.out.println(getTime() + TITTLE + ERROR_PREFIX + msg);
            }
            msg = getTime() + TITTLE + ERROR_PREFIX + msg;
            writeLogToFile(msg);
        }
    }

    public static void error(String format, Object... arguments) {
        if (canLog(Level.SEVERE)) {
            error(format(format, arguments));
        }
    }

    public static void error(Throwable t) {
        if (canLog(Level.FINER) && enablePrintLog) {
            String msg = t.getMessage();
            if (enableColor) {
                System.out.println(getTime() + TITTLE_COLOR_PREFIX + ERROR_COLOR_PREFIX + msg);
            } else {
                System.out.println(getTime() + TITTLE + ERROR_PREFIX + msg);
            }
            msg = getTime() + TITTLE + ERROR_PREFIX + msg;
            StringWriter stringWriter = new StringWriter();
            t.printStackTrace(new PrintWriter(stringWriter));
            msg = msg + stringWriter;
            writeLogToFile(msg);
        }
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

    private static boolean canLog(Level level) {
        return level.intValue() >= LEVEL.intValue();
    }

    private static String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return simpleDateFormat.format(new Date()) + " ";
    }

    private static void writeLogToFile(String msg) {
        FileOutputStream o = null;
        try {
            File file = new File(filePath + "/dongtai_javaagent.log");
            o = new FileOutputStream(file, true);
            o.write(msg.getBytes());
            o.write(System.getProperty("line.separator").getBytes());
            o.flush();
            o.close();
        } catch (Exception e) {
            DongTaiLog.error(e);
        }
    }

    static {
        if ("true".equals(IastProperties.enablePrintLog())) {
            enablePrintLog = true;
        } else if ("false".equals(IastProperties.enablePrintLog())) {
            enablePrintLog = false;
        }
        filePath = IastProperties.getLogPath();
        if (enablePrintLog && !isCreateLog) {
            File f = new File(filePath);
            if (!f.exists()) {
                f.mkdirs();
            }
            File file = new File(filePath, File.separator+"dongtai_javaagent.log");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException ignore) {
                } finally {
                    isCreateLog = true;
                }
            }
        }
    }
}
