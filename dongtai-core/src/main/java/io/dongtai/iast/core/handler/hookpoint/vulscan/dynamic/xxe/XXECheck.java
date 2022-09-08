package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.handler.hookpoint.models.IastSinkModel;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;

import java.util.Arrays;
import java.util.List;

public class XXECheck {
    private static final List<Checker> CHECKS = Arrays.asList(
            new DocumentBuilderCheck(),
            new ApacheXMLParserCheck(),
            new SAXXMLReaderCheck()
    );
    public static boolean isSafe(MethodEvent event, IastSinkModel sink) {
        if (event.object == null) {
            return false;
        }

        for (Checker chk : CHECKS) {
            Object obj = chk.getCheckObject(event);
            if (chk.match(obj)) {
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

        return false;
    }
}
