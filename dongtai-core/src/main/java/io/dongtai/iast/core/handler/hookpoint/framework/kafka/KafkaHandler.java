package io.dongtai.iast.core.handler.hookpoint.framework.kafka;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.SpyDispatcherImpl;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.SourceImpl;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.service.ErrorLogReport;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class KafkaHandler {
    private static ThreadLocal<String> sharedTraceId = new ThreadLocal<String>();

    public static Object beforeSend(Object record) {
        try {
            Class<?> c = Class.forName(" org.apache.kafka.clients.producer.ProducerRecord".substring(1));
            Constructor<?> con = c.getConstructor(String.class, Integer.class, Long.class, Object.class, Object.class, Iterable.class);
            Object rd = con.newInstance((String) record.getClass().getMethod("topic").invoke(record),
                    (Integer) record.getClass().getMethod("partition").invoke(record),
                    (Long) record.getClass().getMethod("timestamp").invoke(record),
                    record.getClass().getMethod("key").invoke(record),
                    record.getClass().getMethod("value").invoke(record),
                    (Iterable<?>) record.getClass().getMethod("headers").invoke(record));

            String traceId = ContextManager.getSegmentId();
            sharedTraceId.set(traceId);
            Object headers = rd.getClass().getMethod("headers").invoke(rd);
            headers.getClass().getMethod("add", String.class, byte[].class).
                    invoke(headers, ContextManager.getHeaderKey(), traceId.getBytes(StandardCharsets.UTF_8));
            return rd;
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
        return record;
    }

    public static void afterSend(Object record, Object ret) {
        if (EngineManager.TAINT_POOL.get().isEmpty()) {
            return;
        }

        MethodEvent event = new MethodEvent(
                0,
                0,
                " org.apache.kafka.clients.producer.KafkaProducer".substring(1),
                " org.apache.kafka.clients.producer.KafkaProducer".substring(1),
                "send",
                " org.apache.kafka.clients.producer.KafkaProducer.send(org.apache.kafka.clients.producer.ProducerRecord<K,V>,org.apache.kafka.clients.producer.Callback)".substring(1),
                " org.apache.kafka.clients.producer.KafkaProducer.send(org.apache.kafka.clients.producer.ProducerRecord<K,V>,org.apache.kafka.clients.producer.Callback)".substring(1),
                null,
                new Object[]{record},
                ret,
                "KAFKA",
                false,
                null
        );

        boolean isHitTaints = TaintPoolUtils.poolContains(record, event, true);
        if (isHitTaints) {
            int invokeId = SpyDispatcherImpl.INVOKE_ID_SEQUENCER.getAndIncrement();
            event.setInvokeId(invokeId);
            event.setPlugin("KAFKA");
            event.setServiceName("");
            event.setTraceId(sharedTraceId.get());
            event.setCallStack(StackUtils.getLatestStack(5));
            EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
            Set<Object> resModelItems = SourceImpl.parseCustomModel(ret);
            Set<Object> resModelSet = new HashSet<Object>();
            for (Object obj : resModelItems) {
                resModelSet.add(obj);
                int identityHashCode = System.identityHashCode(obj);
                event.addTargetHash(identityHashCode);
                event.addTargetHashForRpc(obj.hashCode());
                EngineManager.TAINT_HASH_CODES.get().add(identityHashCode);
            }
            event.outValue = resModelSet;
        }
    }

    public static void afterPoll(Object record) {
        if (record == null) {
            return;
        }

        try {
            Iterator it = (Iterator) record.getClass().getMethod("iterator").invoke(record);
            HashMap<String, String> headerMap = new HashMap<String, String>();

            while (it.hasNext()) {
                EngineManager.SCOPE_TRACKER.enterKafka();
                EngineManager.SCOPE_TRACKER.enterSource();
                Object rd = it.next();
                String topic = (String) rd.getClass().getMethod("topic").invoke(rd);
                String partition = String.valueOf(rd.getClass().getMethod("partition").invoke(rd));
                headerMap = getHeaderMapFromRecord(rd);
                if (headerMap.get(ContextManager.getHeaderKey()) != null) {
                    ContextManager.getOrCreateGlobalTraceId(headerMap.get(ContextManager.getHeaderKey()),
                            EngineManager.getAgentId());
                } else {
                    String traceId = ContextManager.getSegmentId();
                    sharedTraceId.set(traceId);
                }

                Map<String, Object> requestMeta = new HashMap<String, Object>(12);
                requestMeta.put("protocol", "KAFKA");
                requestMeta.put("scheme", "kafka");
                requestMeta.put("method", "CONSUME");
                requestMeta.put("secure", "true");
                requestMeta.put("requestURL", "kafka://" + partition + "/" + topic);
                requestMeta.put("requestURI", "/" + topic);
                requestMeta.put("remoteAddr", "");
                requestMeta.put("queryString", "");
                requestMeta.put("headers", headerMap);
                requestMeta.put("body", "");
                requestMeta.put("contextPath", "");
                requestMeta.put("replay-request", false);

                EngineManager.REQUEST_CONTEXT.set(requestMeta);
                EngineManager.TRACK_MAP.set(new HashMap<Integer, MethodEvent>(1024));
                EngineManager.TAINT_POOL.set(new HashSet<Object>());
                EngineManager.TAINT_HASH_CODES.set(new HashSet<Integer>());

                MethodEvent event = new MethodEvent(
                        0,
                        0,
                        " org.apache.kafka.clients.consumer.KafkaConsumer".substring(1),
                        " org.apache.kafka.clients.consumer.KafkaConsumer".substring(1),
                        "poll",
                        " org.apache.kafka.clients.consumer.KafkaConsumer.poll()".substring(1),
                        " org.apache.kafka.clients.consumer.KafkaConsumer.poll()".substring(1),
                        null,
                        null,
                        rd,
                        "KAFKA",
                        false,
                        null
                );

                int invokeId = SpyDispatcherImpl.INVOKE_ID_SEQUENCER.getAndIncrement();
                event.source = true;
                event.setInvokeId(invokeId);
                event.setPlugin("KAFKA");
                event.setServiceName("MESSAGE_QUEUE");
                event.setTraceId(sharedTraceId.get());
                event.setCallStack(StackUtils.getLatestStack(5));

                Set<Object> resModelSet = new HashSet<Object>();
                resModelSet.add(rd);
                event.outValue = resModelSet;

                EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
                EngineManager.TAINT_POOL.addTaintToPool(rd, event, true);

                EngineManager.SCOPE_TRACKER.leaveSource();
                EngineManager.SCOPE_TRACKER.leaveKafka();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    private static HashMap<String, String> getHeaderMapFromRecord(Object rd) {
        HashMap<String, String> headerMap = new HashMap<String, String>();
        try {
            Object rh = rd.getClass().getMethod("headers").invoke(rd);
            Object[] headers = (Object[]) rh.getClass().getMethod("toArray").invoke(rh);
            for (Object header : headers) {
                headerMap.put((String) header.getClass().getMethod("key").invoke(header),
                        new String((byte[]) header.getClass().getMethod("value").invoke(header)));
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
        return headerMap;
    }
}
