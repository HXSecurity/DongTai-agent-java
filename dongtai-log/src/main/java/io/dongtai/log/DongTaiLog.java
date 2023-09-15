package io.dongtai.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

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

    /**
     * 把错误码放入到这个集合中，打印的时候会限制打印频率，最多每 {@link #FREQUENT_INTERVAL} 打一次
     */
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

        String path = LOG_DIR + File.separator + "dongtai_javaagent-" + agentId + ".log";
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
            prefix += "[" + code + "] ";
        }

        if (cnt > 0) {
            prefix += "[occurred " + cnt + " times] ";
        }

        return prefix;
    }

    private static String getMessage(String msg, Throwable t) {
        if (t != null) {
            if (msg == null || msg.isEmpty()) {
                msg = "Exception: " + t;
            } else {
                msg += ", Exception: " + t;
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

    /**
     * 格式化字符串
     *
     * @param pattern   用来格式化的pattern，比如 "my name is {}, today is {}"
     * @param arguments 用来填充上面的pattern的参数，比如 []String{"CC11001100", "2023-7-12"}
     * @return 格式化之后的字符串，比如 "my name is CC11001100, today is 2023-7-12"
     */
    static String format(String pattern, Object... arguments) {
//        if (from != null) {
//            String computed = from;
//            if (arguments != null && arguments.length != 0) {
//                for (Object argument : arguments) {
//                    computed = computed.replaceFirst("\\{\\}", argument == null ? "NULL" : Matcher.quoteReplacement(argument.toString()));
//                }
//            }
//            return computed;
//        }
//        return null;

        // 如果没有参数格式化的话快速退出
        if (arguments.length == 0 || pattern == null) {
            return pattern;
        }

        // 先把参数都转为字符串，并且预估参数的字符长度 
        int argumentCharCount = 0;
        String[] argumentStrings = new String[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            argumentStrings[i] = arguments[i] == null ? "NULL" : arguments[i].toString();
            argumentCharCount += argumentStrings[i].length();
        }

        // 现在，就能算出结果的长度避免扩容了，这里没有使用减去 参数长度乘以2的占位符长度 是因为pattern中占位符的实际个数可能并不足以消费完参数
        //  （因为并没有强制pattern里的占位符与参数一一对应，所以它们的个数是很随意的），这种情况下如果做乐观假设的话可能会导致特殊情况下扩容，
        //   所以此处采取比较保守的策略，宁愿在特殊情况下浪费几个字节的空间也不愿在特殊情况下扩容
        // 举一个具体的例子，比如传入的pattern: "a{}c", 传入的参数 String[]{"b", "d", .. , "z"}，参数可能有几十个，远超占位符的个数，则此时会被减去错误的长度，甚至导致长度为负
//        StringBuilder buff = new StringBuilder(pattern.length() - argumentStrings.length * 2 + argumentCharCount);
        StringBuilder buff = new StringBuilder(pattern.length() + argumentCharCount);
        // 下一个被消费的参数的下标
        int argumentConsumeIndex = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            switch (c) {
                case '\\':
                    // 如果是转义字符的话，则看下它在转义个啥
                    if (i + 1 < pattern.length()) {
                        // 非最后一个字符，则根据不同的转义保留不同的字符，并且一次消费两个字符
                        char nextChar = pattern.charAt(++i);
                        switch (nextChar) {
                            case '{':
                            case '}':
                                // 只保留被转义的左花括号或者右花括号，转义字符本身将被丢弃
                                buff.append(nextChar);
                                break;
                            default:
                                // 转移的是其它字符，则原样保持转义，相当于是在通用转义的基础上扩展了对左右花括号的转义
                                buff.append(c).append(nextChar);
                                break;
                        }
                    } else {
                        // 最后一个字符了，原样保留
                        buff.append(c);
                    }
                    break;
                case '{':
                    // 如果是左括号的话，则看一下是否是个占位符
                    if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '}') {
                        // 一次消费两个字符，把占位符 {} 消费掉
                        i++;
                        // 使用参数替换
                        buff.append(argumentStrings[argumentConsumeIndex++]);
                        // 实际传递的参数个数和pattern中的占位符数量可能会不匹配，参数更多的时候没问题多余的将被忽略
                        // 参数更少的时候继续也没啥意义了，所以如果参数被消费完了则快速退出
                        if (argumentConsumeIndex == argumentStrings.length) {
                            // 把 } 后边的一股脑儿消费了，如果有的话
                            if (i + 1 < pattern.length()) {
                                buff.append(pattern.substring(i + 1, pattern.length()));
                            }
                            return buff.toString();
                        }
                    } else {
                        // 最后一个字符或者不是匹配的右花括号，原样保留
                        buff.append(c);
                    }
                    break;
                default:
                    // 普通字符，原样复制
                    buff.append(c);
                    break;
            }
        }
        return buff.toString();
    }

    private static String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return simpleDateFormat.format(new Date()) + " ";
    }

    // TODO 2023-7-12 12:00:06 写日志文件效率问题
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
