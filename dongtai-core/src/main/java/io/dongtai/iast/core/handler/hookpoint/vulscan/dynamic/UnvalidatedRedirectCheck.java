package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.IastSinkModel;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange.TaintRange;
import io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange.TaintRanges;
import io.dongtai.iast.core.utils.TaintPoolUtils;
import io.dongtai.log.DongTaiLog;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;

public class UnvalidatedRedirectCheck implements SinkSourceChecker {
    public final static String SINK_TYPE = "unvalidated-redirect";

    private final static String REDIRECT_KEY = "location";
    private final static String REDIRECT_LOWER_KEY = "Location";

    private final static HashSet<String> REDIRECT_SIGNATURES = new HashSet<String>(Arrays.asList(
            " javax.servlet.http.HttpServletResponse.sendRedirect(java.lang.String)".substring(1),
            " jakarta.servlet.http.HttpServletResponse.sendRedirect(java.lang.String)".substring(1),
            " org.glassfish.grizzly.http.server.Response.sendRedirect(java.lang.String)".substring(1),
            " play.mvc.Results.redirect(java.lang.String)".substring(1)
    ));

    private final static HashSet<String> REDIRECT_URI_SIGNATURES = new HashSet<String>(Arrays.asList(
            " javax.ws.rs.core.Response.temporaryRedirect(java.net.URI)".substring(1),
            " jakarta.ws.rs.core.Response.temporaryRedirect(java.net.URI)".substring(1)
    ));

    private final static String NETTY_ADD_HEADER = "io.netty.handler.codec.http.DefaultHttpHeaders.add0(int,int,java.lang.CharSequence,java.lang.CharSequence)";

    private final static HashSet<String> HEADER_SIGNATURES = new HashSet<String>(Arrays.asList(
            " javax.servlet.http.HttpServletResponse.setHeader(java.lang.String,java.lang.String)".substring(1),
            " jakarta.servlet.http.HttpServletResponse.setHeader(java.lang.String,java.lang.String)".substring(1),
            " javax.servlet.http.HttpServletResponse.addHeader(java.lang.String,java.lang.String)".substring(1),
            " jakarta.servlet.http.HttpServletResponse.addHeader(java.lang.String,java.lang.String)".substring(1),
            NETTY_ADD_HEADER
    ));

    public boolean match(IastSinkModel sink) {
        return SINK_TYPE.equals(sink.getType()) && (
                REDIRECT_SIGNATURES.contains(sink.getSignature())
                        || REDIRECT_URI_SIGNATURES.contains(sink.getSignature())
                        || HEADER_SIGNATURES.contains(sink.getSignature())
        );
    }

    public boolean checkSource(MethodEvent event, IastSinkModel sink) {
        if (REDIRECT_SIGNATURES.contains(sink.getSignature())) {
            return checkRedirect(event, sink);
        } else if (REDIRECT_URI_SIGNATURES.contains(sink.getSignature())) {
            return checkRedirectURI(event, sink);
        } else if (HEADER_SIGNATURES.contains(sink.getSignature())) {
            return checkHeader(event, sink);
        }
        return false;
    }

    private static boolean checkRedirect(MethodEvent event, IastSinkModel sink) {
        if (event.argumentArray.length == 0) {
            return false;
        }
        return checkValue(event.argumentArray[0], event);
    }

    private static boolean checkRedirectURI(MethodEvent event, IastSinkModel sink) {
        if (event.argumentArray.length == 0 || !(event.argumentArray[0] instanceof URI)) {
            return false;
        }
        URI uri = (URI) event.argumentArray[0];
        String schema = uri.getScheme();
        return checkValue(schema, event);
    }

    private static boolean checkHeader(MethodEvent event, IastSinkModel sink) {
        int keyPos = 0;
        int valPos = 1;
        if (NETTY_ADD_HEADER.equals(sink.getSignature())) {
            keyPos = 2;
            valPos = 3;
        }

        if (event.argumentArray.length <= valPos) {
            return false;
        }

        try {
            String key = (String) event.argumentArray[keyPos];
            if (!REDIRECT_KEY.equals(key) && !REDIRECT_LOWER_KEY.equals(key)) {
                return false;
            }
            return checkValue(event.argumentArray[valPos], event);
        } catch (Throwable e) {
            DongTaiLog.warn(SINK_TYPE + " check header failed", e);
            return false;
        }
    }

    private static boolean checkValue(Object val, MethodEvent event) {
        if (!TaintPoolUtils.poolContains(val, event)) {
            return false;
        }

        TaintRanges tr = EngineManager.TAINT_RANGES_POOL.get(System.identityHashCode(val));
        if (tr.isEmpty()) {
            return false;
        }

        // only the value prefix of value is not safe
        for (TaintRange t : tr.getTaintRanges()) {
            if (t.getStart() == 0) {
                return true;
            }
        }

        return false;
    }
}
