package io.dongtai.iast.core.handler.hookpoint.service.trace;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FeignService {
    public static void solveSyncInvoke(MethodEvent event, AtomicInteger invokeIdSequencer) {
        try {
            if (event.parameterInstances.length != 1) {
                return;
            }

            Object handlerObj = event.objectInstance;
            Field metadataField = handlerObj.getClass().getDeclaredField("metadata");
            metadataField.setAccessible(true);
            Object metadata = metadataField.get(event.objectInstance);
            Method templateMethod = metadata.getClass().getMethod("template");
            Object template = templateMethod.invoke(metadata);

            // get args
            Object args = event.parameterInstances[0];
            trackObject(event, args, 0);

            boolean hasTaint = false;
            if (!event.getSourceHashes().isEmpty()) {
                hasTaint = true;
            }
            event.addParameterValue(1, args, hasTaint);

            Method addHeaderMethod = template.getClass().getDeclaredMethod("header", String.class, String[].class);
            addHeaderMethod.setAccessible(true);
            String traceId = ContextManager.nextTraceId();
            // clear old traceId header
            addHeaderMethod.invoke(template, ContextManager.getHeaderKey(), new String[]{});
            addHeaderMethod.invoke(template, ContextManager.getHeaderKey(), new String[]{traceId});

            // add to method pool
            event.source = false;
            event.traceId = traceId;
            event.setCallStacks(StackUtils.createCallStack(4));
            int invokeId = invokeIdSequencer.getAndIncrement();
            event.setInvokeId(invokeId);
            EngineManager.TRACK_MAP.get().put(invokeId, event);
        } catch (NoSuchFieldException ignore) {
        } catch (NoSuchMethodException ignore) {
        } catch (Throwable e) {
            DongTaiLog.error("solve feign invoke failed", e);
        }
    }

    private static void trackObject(MethodEvent event, Object obj, int depth) {
        if (depth >= 10 || !TaintPoolUtils.isNotEmpty(obj) || !TaintPoolUtils.isAllowTaintType(obj)) {
            return;
        }

        Class<?> cls = obj.getClass();
        if (cls.isArray() && !cls.getComponentType().isPrimitive()) {
            trackArray(event, obj, depth);
        } else if (obj instanceof Iterator) {
            trackIterator(event, (Iterator<?>) obj, depth);
        } else if (obj instanceof Map) {
            trackMap(event, (Map<?, ?>) obj, depth);
        } else if (obj instanceof Map.Entry) {
            trackMapEntry(event, (Map.Entry<?, ?>) obj, depth);
        } else if (obj instanceof Collection) {
            if (obj instanceof List) {
                trackList(event, (List<?>) obj, depth);
            } else {
                trackIterator(event, ((Collection<?>) obj).iterator(), depth);
            }
        } else if ("java.util.Optional".equals(obj.getClass().getName())) {
            trackOptional(event, obj, depth);
        } else {
            if (!(obj instanceof String)) {
                Object[] getterValues = parseCustomModel(event, obj);
                if (getterValues != null && getterValues.length > 0) {
                    trackArray(event, getterValues, depth);
                }
            }

            int hash = System.identityHashCode(obj);
            if (EngineManager.TAINT_HASH_CODES.contains(hash)) {
                event.addSourceHash(hash);
            }
        }
    }

    private static void trackArray(MethodEvent event, Object arr, int depth) {
        int length = Array.getLength(arr);
        for (int i = 0; i < length; i++) {
            trackObject(event, Array.get(arr, i), depth + 1);
        }
    }

    private static void trackIterator(MethodEvent event, Iterator<?> it, int depth) {
        while (it.hasNext()) {
            trackObject(event, it.next(), depth + 1);
        }
    }

    private static void trackMap(MethodEvent event, Map<?, ?> map, int depth) {
        for (Object key : map.keySet()) {
            trackObject(event, key, depth + 1);
            trackObject(event, map.get(key), depth + 1);
        }
    }

    private static void trackMapEntry(MethodEvent event, Map.Entry<?, ?> entry, int depth) {
        trackObject(event, entry.getKey(), depth + 1);
        trackObject(event, entry.getValue(), depth + 1);
    }

    private static void trackList(MethodEvent event, List<?> list, int depth) {
        for (Object obj : list) {
            trackObject(event, obj, depth + 1);
        }
    }

    private static void trackOptional(MethodEvent event, Object obj, int depth) {
        try {
            Object v = ((Optional<?>) obj).orElse(null);
            trackObject(event, v, depth + 1);
        } catch (Throwable e) {
            DongTaiLog.warn("feign track optional object failed: " + e.getMessage());
        }
    }

    private static Object[] parseCustomModel(MethodEvent event, Object obj) {
        return null;
    }
}
