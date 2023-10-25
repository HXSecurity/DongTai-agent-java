package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SignatureMethodMatcher;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author UzJu
 * @date 2023/10/24 16:02
 * @Site UzzJu.com
 * @Comment :)
 */

public class QLExpressCheck implements SinkSafeChecker {
    public static List<String> QLExpress_SINK_METHODS = Arrays.asList(
            " com.ql.util.express.ExpressRunner.parseInstructionSet(java.lang.String)".substring(1)
    );
    private String policySignature;
    private static ClassLoader QL_CLASS_LOADER;

    @Override
    public boolean match(MethodEvent event, SinkNode sinkNode) {
        if (sinkNode.getMethodMatcher() instanceof SignatureMethodMatcher) {
            this.policySignature = ((SignatureMethodMatcher) sinkNode.getMethodMatcher()).getSignature().toString();
        }

        return QLExpress_SINK_METHODS.contains(this.policySignature);
    }

    @Override
    public boolean isSafe(MethodEvent event, SinkNode sinkNode){
        /**
         * The QLExpress component provides the forbidInvokeSecurityRiskMethods configuration function to enable blacklist detection. Therefore, in addition to determining the sink point, you must also determine if the user has this configuration enabled.
         * If this configuration is enabled, you will be prompted when calling a blacklisted class: com.ql.util.express.exception.QLSecurityRiskException: An unsafe system method was called using QLExpress: public java.lang.Process java. lang.Runtime.exec(java.lang.String) throws java.io.IOException
         * */
        DongTaiLog.debug("Start determining whether the forbidInvokeSecurityRiskMethods field of the QLExpress component is true or not.");
        try {
            Class<?> cls;
            if (QL_CLASS_LOADER == null){
                cls = Class.forName(" com.ql.util.express.config.QLExpressRunStrategy".substring(1));
            }else {
                cls = Class.forName(" com.ql.util.express.config.QLExpressRunStrategy".substring(1), false, QL_CLASS_LOADER);
            }

            Field getBlackListField = cls.getDeclaredField("forbidInvokeSecurityRiskMethods");
            Field getSendBoxModeField = cls.getDeclaredField("sandboxMode");
            Field getWhiteListField = cls.getDeclaredField("SECURE_METHOD_LIST");

            if (Modifier.isStatic(getBlackListField.getModifiers()) && Modifier.isPrivate(getBlackListField.getModifiers()) && Modifier.isStatic(getSendBoxModeField.getModifiers()) && Modifier.isPrivate(getSendBoxModeField.getModifiers()) && Modifier.isStatic(getWhiteListField.getModifiers()) && Modifier.isPrivate(getWhiteListField.getModifiers())) {
                // Make private fields accessible to reflection
                getBlackListField.setAccessible(true);
                getSendBoxModeField.setAccessible(true);
                getWhiteListField.setAccessible(true);

                // get fields value
                boolean blackListBoolean = getBlackListField.getBoolean(null);
                boolean sendBoxBoolean = getSendBoxModeField.getBoolean(null);
                Set<String> secureMethodList = (Set<String>) getWhiteListField.get(null);
                DongTaiLog.debug("SECURE_METHOD_LIST = " + secureMethodList);
                DongTaiLog.debug("sandboxMode = " + sendBoxBoolean);
                DongTaiLog.debug("forbidInvokeSecurityRiskMethods = " + blackListBoolean);
                // All three conditions need to be met
                return (secureMethodList != null && !secureMethodList.isEmpty()) || sendBoxBoolean || blackListBoolean;
            } else {
                DongTaiLog.debug("Field is not static and private.");
                return true;
            }
        }catch (Throwable e){
            DongTaiLog.debug("An error occurred while retrieving the fields of the QLExpress component.: {}, {}",
                    e.getClass().getName() + ": " + e.getMessage(),
                    e.getCause() != null ? e.getCause().getMessage() : "");
             return true;
        }
    }
    public static void setQLClassLoader(ClassLoader qlClassLoader) {
        QL_CLASS_LOADER = qlClassLoader;
    }

    public static void clearQLClassLoader(){
        QL_CLASS_LOADER = null;
    }
}