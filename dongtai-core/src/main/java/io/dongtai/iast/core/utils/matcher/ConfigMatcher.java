package io.dongtai.iast.core.utils.matcher;

import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyManager;
import io.dongtai.iast.core.utils.ConfigUtils;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.log.DongTaiLog;
import org.apache.commons.lang3.StringUtils;

import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 各种匹配方法（通过配置文件匹配）
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ConfigMatcher {

    private static ConfigMatcher INSTANCE;
    private final Set<String> BLACKS_SET;
    private final String[] START_ARRAY;
    private final String[] END_ARRAY;
    private final String[] DISABLE_EXT;
    private Instrumentation inst;

    private final Set<String> BLACK_URL;
    public final Set<String> FALLBACK_URL = new HashSet<String>();

    public static ConfigMatcher getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new ConfigMatcher();
        }
        return INSTANCE;
    }

    public ConfigMatcher() {
        PropertyUtils cfg = PropertyUtils.getInstance();
        String blackList = cfg.getBlackClassFilePath();
        String blackUrl = cfg.getBlackUrl();
        String disableExtList = cfg.getBlackExtFilePath();

        BLACK_URL = ConfigUtils.loadConfigFromFileByLine(blackUrl);

        Set<String>[] items = ConfigUtils.loadConfigFromFile(blackList);
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

    @Deprecated
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
                }
            }
            for (String string : FALLBACK_URL) {
                if (uri.endsWith(string)) {
                    return true;
                }
            }
        } catch (Throwable e) {
            DongTaiLog.trace("config matcher getBlackUrl failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
        return false;
    }

    private boolean inHookBlacklist(String className) {
        return BLACKS_SET.contains(className)
                || StringUtils.startsWithAny(className, START_ARRAY)
                || StringUtils.endsWithAny(className, END_ARRAY);
    }

    public void setInst(Instrumentation inst) {
        this.inst = inst;
    }

    /**
     * @param clazz
     * @return
     * @since 1.4.0
     */
    public boolean canHook(Class<?> clazz, PolicyManager policyManager) {
        if (!inst.isModifiableClass(clazz)) {
            return false;
        }
        if (clazz.isInterface()) {
            return false;
        }
        String className = clazz.getName().replace('.', '/');
        return canHook(className, policyManager);
    }

    /**
     * 判断当前类是否在hook点黑名单。hook黑名单： 1.agent自身的类； 2.已知的框架类、中间件类； 3.类名为null； 4.JDK内部类且不在hook点配置白名单中； 5.接口
     *
     * @param className jvm内部类名，如：java/lang/Runtime
     * @return 是否支持hook
     */
    public boolean canHook(String className, PolicyManager policyManager) {
        if (StringUtils.isEmpty(className)) {
            return false;
        }
        if (className.startsWith("[")) {
            // DongTaiLog.trace("ignore transform {}. Reason: class is a Array Type", className);
            return false;
        }
        if (className.contains("/$Proxy")) {
            // DongTaiLog.trace("ignore transform {}. Reason: classname is a aop class by Proxy", className);
            return false;
        }
        if (className.startsWith("java/lang/iast/")
                || className.startsWith("io/dongtai/")
                || className.startsWith("com/sun/jna/")
        ) {
            // DongTaiLog.trace("ignore transform {}. Reason: class is in blacklist", className);
            return false;
        }
        if (className.contains("CGLIB$$")) {
            // DongTaiLog.trace("ignore transform {}. Reason: classname is a aop class by CGLIB", className);
            return false;
        }

        if (className.contains("$$Lambda$")) {
            // DongTaiLog.trace("ignore transform {}. Reason: classname is a aop class by Lambda", className);
            return false;
        }

        if (className.contains("_$$_jvst")) {
            // DongTaiLog.trace("ignore transform {}. Reason: classname is a aop class", className);
            return false;
        }

        String realClassName = className.replace('/', '.');
        boolean isBlack = inHookBlacklist(className);
        if (isBlack) {
            if (policyManager.getPolicy() == null) {
                return false;
            }
            policyManager.getPolicy().addBlacklistHooks(realClassName);
            if (!policyManager.getPolicy().isIgnoreBlacklistHooks(realClassName)
                    && !policyManager.getPolicy().isIgnoreInternalHooks(realClassName)) {
                // DongTaiLog.trace("ignore transform {}. Reason: classname is startswith com/secnium/iast/", className);
                return false;
            }
        }
        return true;
    }
}

