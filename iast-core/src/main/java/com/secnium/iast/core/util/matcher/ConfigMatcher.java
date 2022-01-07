package com.secnium.iast.core.util.matcher;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.report.ErrorLogReport;
import com.secnium.iast.core.util.ConfigUtils;

import com.secnium.iast.core.util.ThrowableUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import com.secnium.iast.log.DongTaiLog;

/**
 * 各种匹配方法（通过配置文件匹配）
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ConfigMatcher {

    private final static Set<String> BLACKS;
    private final static String[] START_WITH_BLACKS;
    private final static String[] END_WITH_BLACKS;
    private final static Set<String> BLACKS_SET;
    private final static String[] START_ARRAY;
    private final static String[] END_ARRAY;
    private final static String[] DISABLE_EXT;
    private final static AbstractMatcher INTERNAL_CLASS = new InternalClass();
    private final static AbstractMatcher FRAMEWORK_CLASS = new FrameworkClass();
    private final static AbstractMatcher SERVER_CLASS = new ServerClass();

    private final static Set<String> BLACK_URL;


    /**
     * 检查后缀黑名单
     *
     * @param uri
     * @return
     */
    public static boolean disableExtension(String uri) {
        if (uri == null || uri.isEmpty()) {
            return false;
        }
        return StringUtils.endsWithAny(uri, DISABLE_EXT);
    }

    public static boolean getBlackUrl(Map<String, Object> request) {
        try {
            if (request == null || request.isEmpty()) {
                return false;
            }
            String uri = (String) request.get("requestURI");
            HashMap headers = (HashMap) request.get("headers");
            for (String string : BLACK_URL) {
                String[] strings = string.split(" ");
                switch (Integer.parseInt(strings[1])) {
                    case 1:
                        if (uri.contains(strings[0])) {
                            return true;
                        }
                    case 2:
                        if (null != headers.get(strings[0].toLowerCase())) {
                            return true;
                        }
                    default:
                        continue;
                }
            }
        } catch (Exception e) {
            DongTaiLog.info("dongtai getBalckurl error");
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
        return false;
    }

    private static boolean inHookBlacklist(String className) {
        return BLACKS_SET.contains(className)
                || StringUtils.startsWithAny(className, START_ARRAY)
                || StringUtils.endsWithAny(className, END_ARRAY);
    }

    public static PropagatorType blackFunc(final String signature) {
        if (BLACKS.contains(signature)
                || StringUtils.startsWithAny(signature, START_WITH_BLACKS)
                || StringUtils.endsWithAny(signature, END_WITH_BLACKS)) {
            return PropagatorType.BLACK;
        } else {
            return PropagatorType.NONE;
        }
    }

    /**
     * 判断当前类是否在hook点黑名单。hook黑名单： 1.agent自身的类； 2.已知的框架类、中间件类； 3.类名为null； 4.JDK内部类且不在hook点配置白名单中； 5.接口
     *
     * @param className jvm内部类名，如：java/lang/Runtime
     * @param loader    当前类的classLoader
     * @return 是否支持hook
     */
    public static boolean isHookPoint(String className, ClassLoader loader) {
        if (ConfigMatcher.inHookBlacklist(className)) {
            DongTaiLog.trace("ignore transform {} in loader={}. Reason: classname is startswith com/secnium/iast/",
                    className, loader);
            return false;
        }

        if (className.contains("CGLIB$$")) {
            DongTaiLog.trace("ignore transform {} in loader={}. Reason: classname is a aop class by CGLIB", className,
                    loader);
            return false;
        }

        if (className.contains("$$Lambda$")) {
            DongTaiLog.trace("ignore transform {} in loader={}. Reason: classname is a aop class by Lambda", className,
                    loader);
            return false;
        }

        if (className.contains("_$$_jvst")) {
            DongTaiLog.trace("ignore transform {} in loader={}. Reason: classname is a aop class", className, loader);
            return false;
        }
      
        // todo: 计算startsWith、contains与正则匹配的时间损耗
        if (className.startsWith("com/secnium/iast/")
                || className.startsWith("java/lang/iast/")
                || className.startsWith("cn/huoxian/iast/")
        ) {
            DongTaiLog.trace("ignore transform {} in loader={}. Reason: class is in blacklist", className, loader);
            return false;
        }

        return true;
    }

    public static boolean isAppClass(String className) {
        return !(INTERNAL_CLASS.match(className) || FRAMEWORK_CLASS.match(className) || SERVER_CLASS.match(className));
    }

    static {
        final PropertyUtils cfg = PropertyUtils.getInstance();
        String blackListFuncFile = cfg.getBlackFunctionFilePath();
        String blackList = cfg.getBlackClassFilePath();
        String blackUrl = cfg.getBlackUrl();
        String disableExtList = cfg.getBlackExtFilePath();

        HashSet<String>[] items = ConfigUtils.loadConfigFromFile(blackListFuncFile);
        BLACKS = items[0];
        END_WITH_BLACKS = items[2].toArray(new String[0]);
        START_WITH_BLACKS = items[1].toArray(new String[0]);

        BLACK_URL = ConfigUtils.loadConfigFromFileByLine(blackUrl);

        items = ConfigUtils.loadConfigFromFile(blackList);
        START_ARRAY = items[1].toArray(new String[0]);
        END_ARRAY = items[2].toArray(new String[0]);
        BLACKS_SET = items[0];

        DISABLE_EXT = ConfigUtils.loadExtConfigFromFile(disableExtList);

    }

    /**
     * 事件枚举类型
     */
    public enum PropagatorType {

        /**
         * 方法类型:黑名单
         */
        BLACK,

        /**
         * 方法类型:污点源
         */
        SOURCE,

        /**
         * 方法类型:污点终点
         */
        SINK,

        /**
         * 方法类型:普通方法
         */
        NONE
    }

}

