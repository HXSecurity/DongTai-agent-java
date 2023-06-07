package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SignatureMethodMatcher;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintRange;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintRanges;
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

    private String policySignature;

    @Override
    public boolean match(MethodEvent event, SinkNode sinkNode) {
        if (sinkNode.getMethodMatcher() instanceof SignatureMethodMatcher) {
            this.policySignature = ((SignatureMethodMatcher) sinkNode.getMethodMatcher()).getSignature().toString();
        }

        return SINK_TYPE.equals(sinkNode.getVulType()) && (
                REDIRECT_SIGNATURES.contains(this.policySignature)
                        || REDIRECT_URI_SIGNATURES.contains(this.policySignature)
                        || HEADER_SIGNATURES.contains(this.policySignature)
        );
    }

    @Override
    public boolean checkSource(MethodEvent event, SinkNode sinkNode) {
        if (REDIRECT_SIGNATURES.contains(this.policySignature)) {
            return checkRedirect(event, sinkNode);
        } else if (REDIRECT_URI_SIGNATURES.contains(this.policySignature)) {
            return checkRedirectURI(event, sinkNode);
        } else if (HEADER_SIGNATURES.contains(this.policySignature)) {
            return checkHeader(event, sinkNode);
        }
        return false;
    }

    private boolean checkRedirect(MethodEvent event, SinkNode sinkNode) {
        if (event.parameterInstances.length == 0) {
            return false;
        }
        boolean paramHasTaint = checkValue(event.parameterInstances[0], event);
        if (paramHasTaint) {
            event.addParameterValue(0, event.parameterInstances[0], true);
        }
        return paramHasTaint;
    }

    private boolean checkRedirectURI(MethodEvent event, SinkNode sinkNode) {
        if (event.parameterInstances.length == 0 || !(event.parameterInstances[0] instanceof URI)) {
            return false;
        }
        URI uri = (URI) event.parameterInstances[0];
        String schema = uri.getScheme();
        boolean paramHasTaint = checkValue(schema, event);
        if (paramHasTaint) {
            event.addParameterValue(0, event.parameterInstances[0], true);
        }
        return paramHasTaint;
    }

    private boolean checkHeader(MethodEvent event, SinkNode sinkNode) {
        int keyPos = 0;
        int valPos = 1;
        if (NETTY_ADD_HEADER.equals(this.policySignature)) {
            keyPos = 2;
            valPos = 3;
        }

        if (event.parameterInstances.length <= valPos) {
            return false;
        }

        try {
            String key = (String) event.parameterInstances[keyPos];
            if (!REDIRECT_KEY.equals(key) && !REDIRECT_LOWER_KEY.equals(key)) {
                return false;
            }
            boolean paramHasTaint = checkValue(event.parameterInstances[valPos], event);
            if (paramHasTaint) {
                event.addParameterValue(valPos, event.parameterInstances[valPos], true);
            }
            return paramHasTaint;
        } catch (Throwable e) {
            DongTaiLog.debug(SINK_TYPE + " check header failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
            return false;
        }
    }

    private boolean checkValue(Object val, MethodEvent event) {
        if (!TaintPoolUtils.poolContains(val, event)) {
            return false;
        }
        long hash = TaintPoolUtils.getStringHash(val);
        TaintRanges tr = EngineManager.TAINT_RANGES_POOL.get(hash);
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
