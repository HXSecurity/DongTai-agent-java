package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SignatureMethodMatcher;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;
import io.dongtai.log.DongTaiLog;
import org.apache.maven.artifact.versioning.ComparableVersion;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FastjsonCheck implements SinkSafeChecker {
    public static List<String> FASTJSON_SINK_METHODS = Arrays.asList(
            " com.alibaba.fastjson.JSON.parseObject(java.lang.String)".substring(1),
            " com.alibaba.fastjson.JSON.parse(java.lang.String,int)".substring(1),
            " com.alibaba.fastjson.JSON.parse(java.lang.String)".substring(1)
    );

    private String policySignature;

    private static ClassLoader JSON_CLASS_LOADER;
    private static ClassLoader PARSE_CONFIG_CLASS_LOADER;

    @Override
    public boolean match(MethodEvent event, SinkNode sinkNode) {
        if (sinkNode.getMethodMatcher() instanceof SignatureMethodMatcher) {
            this.policySignature = ((SignatureMethodMatcher) sinkNode.getMethodMatcher()).getSignature().toString();
        }

        return FASTJSON_SINK_METHODS.contains(this.policySignature);
    }

    @Override
    public boolean isSafe(MethodEvent event, SinkNode sinkNode) {
        try {
            Class<?> cls;
            if (JSON_CLASS_LOADER == null) {
                cls = Class.forName(" com.alibaba.fastjson.JSON".substring(1));
            } else {
                cls = Class.forName(" com.alibaba.fastjson.JSON".substring(1), false, JSON_CLASS_LOADER);
            }
            Field f = cls.getDeclaredField("VERSION");
            Class<?> t = f.getType();
            if (t != String.class) {
                return true;
            }
            String version = (String) f.get(null);
            // 1.2.76 to 1.2.80 VERSION is always 1.2.76
            ComparableVersion currentVer = new ComparableVersion(version);
            ComparableVersion safeVer = new ComparableVersion("1.2.83");
            ComparableVersion lowVer = new ComparableVersion("1.2.68");

            if (currentVer.compareTo(safeVer) >= 0) {
                return true;
            }
            if (lowVer.compareTo(currentVer) > 0) {
                return false;
            }

            // https://github.com/alibaba/fastjson/wiki/fastjson_safemode
            Class<?> cfgClass;
            if (PARSE_CONFIG_CLASS_LOADER == null) {
                cfgClass = Class.forName(" com.alibaba.fastjson.parser.ParserConfig".substring(1));
            } else {
                cfgClass = Class.forName(" com.alibaba.fastjson.parser.ParserConfig".substring(1), false, PARSE_CONFIG_CLASS_LOADER);
            }
            Object cfg = cfgClass.getMethod("getGlobalInstance").invoke(null);
            Object isSafeMode = cfg.getClass().getMethod("isSafeMode").invoke(cfg);
            return isSafeMode != null && (Boolean) isSafeMode;
        } catch (Throwable e) {
            DongTaiLog.debug("fastjson version and safe mode check failed: {}, {}",
                    e.getClass().getName() + ": " + e.getMessage(),
                    e.getCause() != null ? e.getCause().getMessage() : "");
            return true;
        }
    }

    public static void setJsonClassLoader(ClassLoader jsonClassLoader) {
        JSON_CLASS_LOADER = jsonClassLoader;
    }

    public static void setParseConfigClassLoader(ClassLoader parseConfigClassLoader) {
        PARSE_CONFIG_CLASS_LOADER = parseConfigClassLoader;
    }

    public static void clearJsonClassLoader(){
        JSON_CLASS_LOADER = null;
    }

    public static void clearParseConfigClassLoader(){
        PARSE_CONFIG_CLASS_LOADER = null;
    }
}
