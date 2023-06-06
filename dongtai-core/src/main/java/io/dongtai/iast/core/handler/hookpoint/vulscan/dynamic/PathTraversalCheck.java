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

public class PathTraversalCheck implements SinkSourceChecker {
    public final static String SINK_TYPE = "path-traversal";

    private final static String NIO_FS_GET_PATH = "java.nio.file.FileSystem.getPath(java.lang.String,java.lang.String[])";

    private final static HashSet<String> SIGNATURES = new HashSet<String>(Arrays.asList(
            NIO_FS_GET_PATH,
            "java.io.File.<init>(java.lang.String)",
            "java.io.File.<init>(java.lang.String,java.lang.String)",
            "java.io.File.<init>(java.io.File,java.lang.String)"
    ));

    private final static HashSet<String> URI_SIGNATURES = new HashSet<String>(Arrays.asList(
            "java.nio.file.spi.FileSystemProvider.newFileSystem(java.net.URI,java.util.Map)",
            "java.nio.file.spi.FileSystemProvider.getFileSystem(java.net.URI)",
            "java.io.File.<init>(java.net.URI)"
    ));

    private String policySignature;

    @Override
    public boolean match(MethodEvent event, SinkNode sinkNode) {
        if (sinkNode.getMethodMatcher() instanceof SignatureMethodMatcher) {
            this.policySignature = ((SignatureMethodMatcher) sinkNode.getMethodMatcher()).getSignature().toString();
        }

        return SINK_TYPE.equals(sinkNode.getVulType()) && (
                SIGNATURES.contains(this.policySignature)
                        || URI_SIGNATURES.contains(this.policySignature)
                        || NIO_FS_GET_PATH.equals(this.policySignature)
        );
    }

    @Override
    public boolean checkSource(MethodEvent event, SinkNode sinkNode) {
        if (SIGNATURES.contains(this.policySignature)) {
            return checkPathArgument(event, sinkNode);
        } else if (URI_SIGNATURES.contains(this.policySignature)) {
            return checkURI(event, sinkNode);
        }
        return false;
    }

    private boolean checkPathArgument(MethodEvent event, SinkNode sinkNode) {
        try {
            int parameterIndex;
            boolean paramHasTaint;
            if (NIO_FS_GET_PATH.equals(this.policySignature)) {
                if (event.parameterInstances.length < 1) {
                    return false;
                }
                if (event.parameterInstances.length == 1) {
                    parameterIndex = 0;
                    paramHasTaint = checkPath((String) event.parameterInstances[0], event);
                } else {
                    String[] paths = (String[]) event.parameterInstances[1];
                    if (paths.length == 0) {
                        parameterIndex = 0;
                        paramHasTaint = checkPath((String) event.parameterInstances[0], event);
                    } else {
                        parameterIndex = 1;
                        paramHasTaint = checkPath(paths[paths.length - 1], event);
                    }
                }
            } else {
                parameterIndex = event.parameterInstances.length - 1;
                paramHasTaint = checkPath((String) event.parameterInstances[parameterIndex], event);
            }

            if (paramHasTaint) {
                event.addParameterValue(parameterIndex, event.parameterInstances[parameterIndex], true);
            }
            return paramHasTaint;
        } catch (Throwable e) {
            DongTaiLog.debug(SINK_TYPE + " check path failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
            return false;
        }
    }

    private boolean checkURI(MethodEvent event, SinkNode sinkNode) {
        if (event.parameterInstances.length == 0 || !(event.parameterInstances[0] instanceof URI)) {
            return false;
        }

        URI uri = (URI) event.parameterInstances[0];
        boolean paramHasTaint = checkPath(uri.getPath(), event);
        if (paramHasTaint) {
            event.addParameterValue(0, event.parameterInstances[0], true);
        }
        return paramHasTaint;

    }

    private boolean checkPath(String path, MethodEvent event) {
        if (!TaintPoolUtils.poolContains(path, event)) {
            return false;
        }

        TaintRanges tr = EngineManager.TAINT_RANGES_POOL.get(TaintPoolUtils.toStringHash(path.hashCode(),System.identityHashCode(path)));
        if (tr.isEmpty()) {
            return false;
        }

        int len = path.length();
        // only the value suffix of value is not safe
        for (TaintRange t : tr.getTaintRanges()) {
            if (t.getStop() == len) {
                return true;
            }
        }

        return false;
    }
}
