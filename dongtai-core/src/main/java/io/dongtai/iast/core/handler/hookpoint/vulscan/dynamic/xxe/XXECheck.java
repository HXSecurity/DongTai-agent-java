package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.handler.hookpoint.models.IastSinkModel;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.SinkSafeChecker;
import io.dongtai.log.DongTaiLog;

import java.util.*;

public class XXECheck implements SinkSafeChecker {
    public final static String SINK_TYPE = "xxe";

    private static final Set<XXEChecker> CHECKS = new HashSet<XXEChecker>(Arrays.asList(
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
    public boolean match(IastSinkModel sink) {
        return SINK_TYPE.equals(sink.getType());
    }

    @Override
    public boolean isSafe(MethodEvent event, IastSinkModel sink) {
        if (event.object == null) {
            return false;
        }

        for (XXEChecker chk : CHECKS) {
            chk.setSourceObjectAndParameters(event.object, event.argumentArray);
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
