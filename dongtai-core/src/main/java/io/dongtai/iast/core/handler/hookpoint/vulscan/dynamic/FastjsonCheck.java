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
            "com.alibaba.fastjson.JSON.parseObject(java.lang.String)",
            "com.alibaba.fastjson.JSON.parse(java.lang.String,int)",
            "com.alibaba.fastjson.JSON.parse(java.lang.String)"
    );

    private String policySignature;

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
            Class<?> cls = Class.forName("com.alibaba.fastjson.JSON");
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
            Class<?> cfgClass = Class.forName("com.alibaba.fastjson.parser.ParserConfig");
            Object cfg = cfgClass.getMethod("getGlobalInstance").invoke(null);
            Object isSafeMode = cfg.getClass().getMethod("isSafeMode").invoke(cfg);
            return isSafeMode != null && (Boolean) isSafeMode;
        } catch (Throwable e) {
            DongTaiLog.warn("fastjson version and safe mode check failed: " + e.getMessage());
            return true;
        }
    }
}
