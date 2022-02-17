package io.dongtai.iast.core.utils.matcher;

import io.dongtai.iast.core.service.ErrorLogReport;
import io.dongtai.iast.core.utils.ConfigUtils;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.utils.ThrowableUtils;
import io.dongtai.log.DongTaiLog;
import org.apache.commons.lang3.StringUtils;

import java.lang.instrument.Instrumentation;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Set;

/**
 * 各种匹配方法（通过配置文件匹配）
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ConfigMatcher {

    private static ConfigMatcher INSTANCE;
    private final Set<String> BLACKS;
    private final String[] START_WITH_BLACKS;
    private final String[] END_WITH_BLACKS;
    private final Set<String> BLACKS_SET;
    private final String[] START_ARRAY;
    private final String[] END_ARRAY;
    private final String[] DISABLE_EXT;
    private final AbstractMatcher INTERNAL_CLASS = new InternalClass();
    private final AbstractMatcher FRAMEWORK_CLASS = new FrameworkClass();
    private final AbstractMatcher SERVER_CLASS = new ServerClass();
    private Instrumentation inst;

    private final Set<String> BLACK_URL;

    public static ConfigMatcher getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new ConfigMatcher();
        }
        return INSTANCE;
    }

    public ConfigMatcher() {
        PropertyUtils cfg = PropertyUtils.getInstance();
        String blackListFuncFile = cfg.getBlackFunctionFilePath();
        String blackList = cfg.getBlackClassFilePath();
        String blackUrl = cfg.getBlackUrl();
        String disableExtList = cfg.getBlackExtFilePath();

        Set<String>[] items = ConfigUtils.loadConfigFromFile(blackListFuncFile);
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
     * 检查后缀黑名单
     *
     * @param uri
     * @return
     */
    public boolean disableExtension(String uri) {
        if (uri == null || uri.isEmpty()) {
            return false;
        }
        return StringUtils.endsWithAny(uri, DISABLE_EXT);
    }

    public boolean getBlackUrl(Map<String, Object> request) {
        try {
            if (request == null || request.isEmpty()) {
                return false;
            }
            String uri = (String) request.get("requestURI");
            Map<String, String> headers = (Map<String, String>) request.get("headers");
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

    private boolean inHookBlacklist(String className) {
        return BLACKS_SET.contains(className)
                || StringUtils.startsWithAny(className, START_ARRAY)
                || StringUtils.endsWithAny(className, END_ARRAY);
    }

    public PropagatorType blackFunc(final String signature) {
        if (BLACKS.contains(signature)
                || StringUtils.startsWithAny(signature, START_WITH_BLACKS)
                || StringUtils.endsWithAny(signature, END_WITH_BLACKS)) {
            return PropagatorType.BLACK;
        } else {
            return PropagatorType.NONE;
        }
    }

    public void setInst(Instrumentation inst) {
        this.inst = inst;
    }

    /**
     * @param clazz
     * @return
     * @since 1.4.0
     */
    public boolean isHookClassPoint(Class<?> clazz) {
        if (!inst.isModifiableClass(clazz)) {
            return false;
        }
        if (clazz.isInterface()) {
            return false;
        }
        String className = clazz.getName().replace('.', '/');
        return isHookPoint(className);
    }

    /**
     * 判断当前类是否在hook点黑名单。hook黑名单： 1.agent自身的类； 2.已知的框架类、中间件类； 3.类名为null； 4.JDK内部类且不在hook点配置白名单中； 5.接口
     *
     * @param className jvm内部类名，如：java/lang/Runtime
     * @return 是否支持hook
     */
    public boolean isHookPoint(String className) {
        if (className.startsWith("[")) {
            DongTaiLog.trace("ignore transform {}. Reason: class is a Array Type", className);
            return false;
        }
        if (className.contains("/$Proxy")) {
            DongTaiLog.trace("ignore transform {}. Reason: classname is a aop class by Proxy", className);
            return false;
        }
        if (className.startsWith("com/secnium/iast/")
                || className.startsWith("java/lang/iast/")
                || className.startsWith("cn/huoxian/iast/")
                || className.startsWith("io/dongtai/")
                || className.startsWith("oshi/")
                || className.startsWith("com/sun/jna/")
        ) {
            DongTaiLog.trace("ignore transform {}. Reason: class is in blacklist", className);
            return false;
        }
        if (inHookBlacklist(className)) {
            DongTaiLog.trace("ignore transform {}. Reason: classname is startswith com/secnium/iast/", className);
            return false;
        }
        if (className.contains("CGLIB$$")) {
            DongTaiLog.trace("ignore transform {}. Reason: classname is a aop class by CGLIB", className);
            return false;
        }

        if (className.contains("$$Lambda$")) {
            DongTaiLog.trace("ignore transform {}. Reason: classname is a aop class by Lambda", className);
            return false;
        }

        if (className.contains("_$$_jvst")) {
            DongTaiLog.trace("ignore transform {}. Reason: classname is a aop class", className);
            return false;
        }
        return true;
    }

    public boolean isAppClass(String className) {
        return !(INTERNAL_CLASS.match(className) || FRAMEWORK_CLASS.match(className) || SERVER_CLASS.match(className));
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

