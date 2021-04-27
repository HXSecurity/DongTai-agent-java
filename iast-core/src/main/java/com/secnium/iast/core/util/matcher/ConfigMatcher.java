package com.secnium.iast.core.util.matcher;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.enhance.IastClassFileTransformer;
import com.secnium.iast.core.util.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * 各种匹配方法（通过配置文件匹配）
 * @author dongzhiyong@huoxian.cn
 */
public class ConfigMatcher {

    private final static Logger logger = com.secnium.iast.core.AgentEngine.DEFAULT_LOGGERCONTEXT.getLogger(ConfigMatcher.class);

    public final static HashSet<String> SOURCES;
    private final static HashSet<String> BLACKS;
    private final static String[] START_WITH_BLACKS;
    private final static String[] END_WITH_BLACKS;
    private final static Set<String> BLACKS_SET;
    private final static String[] START_ARRAY;
    private final static String[] END_ARRAY;
    private final static HashSet<String> internalWhiteList;
    private final static String[] disableExt;
    private final static AbstractMatcher internalClass = new InternalClass();
    private final static AbstractMatcher frameworkClass = new FrameworkClass();
    private final static AbstractMatcher serverClass = new ServerClass();
    private final static ClassLoader iastClassLoader = IastClassFileTransformer.class.getClassLoader();


    public static boolean disableExtention(String uri) {
        if (uri == null || uri.isEmpty()) {
            return false;
        }
        return StringUtils.endsWithAny(uri, disableExt);
    }

    public static boolean internalMatch(String classname) {
        return internalWhiteList.contains(classname);
    }

    private static boolean inHookBlacklist(String className, boolean internal) {
        if (internal) {
            return StringUtils.startsWithAny(className, START_ARRAY)
                    || StringUtils.endsWithAny(className, END_ARRAY)
                    || BLACKS_SET.contains(className);
        } else {
            String internalClassName = className.replace(".", "/");
            return StringUtils.startsWithAny(internalClassName, START_ARRAY)
                    || StringUtils.endsWithAny(internalClassName, END_ARRAY)
                    || BLACKS_SET.contains(internalClassName);
        }
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
     * 判断当前类是否在hook点黑名单。hook黑名单：
     * 1.agent自身的类；
     * 2.已知的框架类、中间件类；
     * 3.类名为null；
     * 4.JDK内部类且不在hook点配置白名单中；
     * 5.接口
     *
     * @param className jvm内部类名，如：java/lang/Runtime
     * @param loader    当前类的classLoader
     * @return 是否支持hook
     */
    public static boolean isHookPoint(String className, ClassLoader loader) {
        boolean hook = true;
        // 类名为null的不hook
        if (className == null) {
            hook = false;
        }

        // com.secnium.iast的类不hook
        // todo: 计算startsWith、contains与正则匹配的时间损耗
        if (hook && (className.startsWith("com/secnium/iast/") || className.startsWith("java/lang/iast/"))) {
            hook = false;
            logger.trace("ignore transform {} in loader={}. Reason: classname is startswith com/secnium/iast/", className, loader);
        }

        // CGLIB$$的类不hook
        if (hook && className.contains("CGLIB$$")) {
            hook = false;
            logger.trace("ignore transform {} in loader={}. Reason: classname is a aop class by CGLIB", className, loader);
        }

        //$$Lambda$ 针对Lambda进行hook会导致Spring框架中出现多个NoClassDefFoundError错误
        if (hook && className.contains("$$Lambda$")) {
            hook = false;
            logger.trace("ignore transform {} in loader={}. Reason: classname is a aop class by Lambda", className, loader);
        }

        //$$Lambda$
        if (hook && className.contains("_$$_jvst")) {
            hook = false;
            logger.trace("ignore transform {} in loader={}. Reason: classname is a aop class", className, loader);
        }


        // 这里过滤掉Sandbox所需要的类，防止ClassCircularityError的发生
        if (hook && ConfigMatcher.inHookBlacklist(className, true)) {
            hook = false;
            logger.trace("ignore transform {} in loader={}. reason: class is in blacklist", className, loader);
        }
        return hook;
    }

    public static boolean isAppClass(String classname) {
        return !(internalClass.match(classname) || frameworkClass.match(classname) || serverClass.match(classname));
    }

    static {
        final PropertyUtils cfg = PropertyUtils.getInstance();
        String sourcesFile = cfg.getSourceFilePath();
        String blackListFuncFile = cfg.getBlackFunctionFilePath();
        String blackList = cfg.getBlackClassFilePath();
        String whiteList = cfg.getWhiteClassFilePath();
        String disableExtList = cfg.getBlackExtFilePath();

        SOURCES = ConfigUtils.loadConfigFromFile(sourcesFile)[0];

        HashSet<String>[] items = ConfigUtils.loadConfigFromFile(blackListFuncFile);
        BLACKS = items[0];
        END_WITH_BLACKS = items[2].toArray(new String[0]);
        START_WITH_BLACKS = items[1].toArray(new String[0]);

        items = ConfigUtils.loadConfigFromFile(blackList);
        START_ARRAY = items[1].toArray(new String[0]);
        END_ARRAY = items[2].toArray(new String[0]);
        BLACKS_SET = items[0];

        items = ConfigUtils.loadConfigFromFile(whiteList);
        internalWhiteList = items[0];

        disableExt = ConfigUtils.loadExtConfigFromFile(disableExtList);

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

