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

public class PathTraversalCheck {
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

    public static boolean isSinkMethod(IastSinkModel sink) {
        return SIGNATURES.contains(sink.getSignature())
                || URI_SIGNATURES.contains(sink.getSignature()) ||
                NIO_FS_GET_PATH.equals(sink.getSignature());
    }

    public static boolean sourceHitTaintPool(MethodEvent event, IastSinkModel sink) {
        if (SIGNATURES.contains(sink.getSignature())) {
            return pathCheck(event, sink);
        } else if (URI_SIGNATURES.contains(sink.getSignature())) {
            return uriCheck(event, sink);
        }
        return false;
    }

    private static boolean pathCheck(MethodEvent event, IastSinkModel sink) {
        try {
            if (NIO_FS_GET_PATH.equals(sink.getSignature())) {
                if (event.argumentArray.length < 2) {
                    return false;
                }
                String[] paths = (String[]) event.argumentArray[1];
                if (paths.length == 0) {
                    return check((String) event.argumentArray[0], event);
                }
                return check(paths[paths.length - 1], event);
            }

            return check((String) event.argumentArray[event.argumentArray.length-1], event);
        } catch (Throwable e) {
            DongTaiLog.warn(SINK_TYPE + " check path failed", e);
            return false;
        }
    }

    private static boolean uriCheck(MethodEvent event, IastSinkModel sink) {
        if (event.argumentArray.length == 0 || !(event.argumentArray[0] instanceof URI)) {
            return false;
        }

        URI uri = (URI) event.argumentArray[0];
        return check(uri.getPath(), event);
    }

    private static boolean check(String path, MethodEvent event) {
        if (!TaintPoolUtils.poolContains(path, event)) {
            return false;
        }

        TaintRanges tr = EngineManager.TAINT_RANGES_POOL.get(System.identityHashCode(path));
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