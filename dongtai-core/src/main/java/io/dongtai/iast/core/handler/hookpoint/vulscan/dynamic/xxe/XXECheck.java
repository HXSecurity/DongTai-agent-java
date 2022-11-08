package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;
import io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.SinkSafeChecker;
import io.dongtai.log.DongTaiLog;

import java.util.*;

public class XXECheck implements SinkSafeChecker {
    public final static String SINK_TYPE = "xxe";

    private static final Set<XXEChecker> CHECKS = new LinkedHashSet<XXEChecker>(Arrays.asList(
            new XMLStreamReaderCheck(),
            new XomCheck(),
            new DocumentBuilderCheck(),
            new JavaxSAXParserCheck(),
            new ApacheXMLParserCheck(),
            new XMLUnmarshallerCheck(),
            new SAXXMLReaderCheck(),
            new XMLInputFactoryCheck()
    ));

    @Override
    public boolean match(MethodEvent event, SinkNode sinkNode) {
        return SINK_TYPE.equals(sinkNode.getVulType());
    }

    @Override
    public boolean isSafe(MethodEvent event, SinkNode sinkNode) {
        if (event.objectInstance == null) {
            return false;
        }

        for (XXEChecker chk : CHECKS) {
            chk.setSourceObjectAndParameters(event.objectInstance, event.parameterInstances);
            List<Object> objs = chk.getCheckObjects();
            for (Object obj : objs) {
                if (chk.match(obj)) {
                    DongTaiLog.trace("xxe check {} match {}", obj.getClass().getName(), chk.getClass().getName());
                    Support support = chk.getSupport(obj);
                    switch (support) {
                        case ALLOWED:
                            return false;
                        case DISALLOWED:
                            return true;
                        default:
                    }
                }
            }
        }

        return false;
    }

    public static XXEChecker getChecker(Object obj) {
        for (XXEChecker chk : CHECKS) {
            if (chk.match(obj)) {
                return chk;
            }
        }
        return null;
    }
}
