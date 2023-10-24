package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SignatureMethodMatcher;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

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
         * Die QLExpress-Komponente bietet die Konfigurationsfunktion forbidInvokeSecurityRiskMethods, um die Erkennung von schwarzen Listen zu ermöglichen. Daher muss zusätzlich zur Bestimmung des Senkenpunkts auch festgestellt werden, ob der Benutzer diese Konfiguration aktiviert hat.
         * Wenn diese Konfiguration aktiviert ist, werden Sie beim Aufruf einer auf der schwarzen Liste stehenden Klasse aufgefordert: com.ql.util.express.exception.QLSecurityRiskException: Eine unsichere Systemmethode wurde mit QLExpress aufgerufen: public java.lang.Process java. lang.Runtime.exec(java.lang.String) throws java.io.IOException
         * */
        DongTaiLog.debug("Start der Ermittlung, ob das Feld forbidInvokeSecurityRiskMethods der QLExpress-Komponente wahr ist oder nicht");
        try {
            Class<?> cls;
            if (QL_CLASS_LOADER == null){
                cls = Class.forName(" com.ql.util.express.config.QLExpressRunStrategy".substring(1));
            }else {
                cls = Class.forName(" com.ql.util.express.config.QLExpressRunStrategy".substring(1), false, QL_CLASS_LOADER);
            }
            Field field = cls.getDeclaredField("forbidInvokeSecurityRiskMethods");
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isPrivate(field.getModifiers())) {
                field.setAccessible(true);
                boolean value = field.getBoolean(null);
                DongTaiLog.debug("forbidInvokeSecurityRiskMethods = " + value);
                return value;
            } else {
                DongTaiLog.debug("Field is not static and private.");
                return true;
            }
        }catch (Throwable e){
            DongTaiLog.debug("Beim Abrufen der Felder der QLExpress-Komponente ist ein Fehler aufgetreten.: {}, {}",
                    e.getClass().getName() + ": " + e.getMessage(),
                    e.getCause() != null ? e.getCause().getMessage() : "");
             return true;
        }
    }
    public static void setQLClassLoader(ClassLoader qlClassLoader) {
        QL_CLASS_LOADER = qlClassLoader;
    }
}