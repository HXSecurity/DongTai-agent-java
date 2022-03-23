package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.api.KrpcApiThread;
import io.dongtai.iast.core.handler.hookpoint.models.KrpcApiModel;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.base64.Base64Encoder;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class KrpcImpl {

    private static final PropertyUtils PROPERTIES = PropertyUtils.getInstance();

    public static void solveKrpc(MethodEvent event, AtomicInteger invokeIdSequencer) {
        // todo handler traceId
        Object ctx = event.argumentArray[0];
        Object req = event.argumentArray[1];
        String krpcService = getUrl(ctx);
        String uri = getUri(req);
        Map<String, String> attachments = getAttachments(req);
        EngineManager.enterKrpcEntry(krpcService, uri, attachments);

        if (EngineManager.isEnterHttp()) {
            return;
        }

        event.source = true;
        event.setCallStacks(StackUtils.createCallStack(6));

        int invokeId = invokeIdSequencer.getAndIncrement();
        event.setInvokeId(invokeId);
        event.inValue = "";
        //todo: outValue 修改为用户数据相关的内容 或 null

        try {
            handlerCustomModel(event, req.getClass().getMethod("asMessage").invoke(req));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            DongTaiLog.error(e);
        }
        EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
    }

    public static void solveKrpcHttpEnter(MethodEvent event, AtomicInteger invokeIdSequencer) {
        Object req = event.argumentArray[1];
        EngineManager.enterKrpcHttp();
        event.source = true;
        event.setCallStacks(StackUtils.createCallStack(6));
        int invokeId = invokeIdSequencer.getAndIncrement();
        event.setInvokeId(invokeId);
        event.inValue = "";
        try {
            addTaintToPool(req.getClass().getMethod("getParameters").invoke(req), event, true);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            DongTaiLog.error(e);
        }
        EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
    }

    public static void solveKrpcHttpExit(MethodEvent event, AtomicInteger invokeIdSequencer) {
        Object req = event.argumentArray[1];
        Class<?> classOfDefaultWebReq = req.getClass();
        String host = getHost(classOfDefaultWebReq, req);
        String path = getPath(classOfDefaultWebReq, req);
        String reqUrl= host+path;
        Map<String,String> reqHeader = getReqHeader(classOfDefaultWebReq, req);
        String reqContent = getReqContent(classOfDefaultWebReq, req);
        String method = getMethod(classOfDefaultWebReq, req);
        String queryString = getQueryString(classOfDefaultWebReq, req);

        Object res= event.argumentArray[2];
        Class<?> classOfDefaultWebRes = res.getClass();
        String resContent = getResContent(classOfDefaultWebRes, res);
        String text = getText(classOfDefaultWebRes, res);
        String httpCode = getHttpCode(classOfDefaultWebRes, res);
        Map<String,String> resHeader = getResHeader(classOfDefaultWebRes, res);

        EngineManager.exitKrpcHttp(host,reqUrl,reqHeader,reqContent,resContent,text,httpCode,method,queryString,path,resHeader);
    }

    private static String getQueryString(Class<?> classOfDefaultWebReq, Object req) {
        try {
            Method getHost = classOfDefaultWebReq.getMethod("getQueryString");
            return String.valueOf(getHost.invoke(req));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return "";
        }
    }

    private static String getMethod(Class<?> classOfDefaultWebReq, Object req) {
        try {
            Method getHost = classOfDefaultWebReq.getMethod("getMethod");
            return String.valueOf(getHost.invoke(req));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return "";
        }
    }

    private static String getHost(Class<?> classOfDefaultWebReq, Object req) {

        try {
            Method getHost = classOfDefaultWebReq.getMethod("getHost");
            return String.valueOf(getHost.invoke(req));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return "";
        }
    }

    private static String getHttpCode(Class<?> classOfDefaultWebRes, Object res) {
        try {
            Method getHttpCode = classOfDefaultWebRes.getMethod("getHttpCode");
            return String.valueOf(getHttpCode.invoke(res));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return "";
        }
    }

    private static String getText(Class<?> classOfDefaultWebRes, Object res) {
        try {
            Method getVersion = classOfDefaultWebRes.getMethod("getVersion");
            Object version = getVersion.invoke(res);
            return String.valueOf(version);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return "";
        }

    }

    private static String getResContent(Class<?> classOfDefaultWebRes, Object res) {
        try {
            Method getContent = classOfDefaultWebRes.getMethod("getContent");
            return String.valueOf(getContent.invoke(res));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return "";
        }
    }

    private static String getReqContent(Class<?> classOfDefaultWebReq, Object req) {
        try {
            Method getContent = classOfDefaultWebReq.getMethod("getContent");
            return String.valueOf(getContent.invoke(req));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return "";
        }
    }

    private static Map<String,String> getReqHeader(Class<?> classOfDefaultWebReq, Object req) {
        try {
            Method getHeaders = classOfDefaultWebReq.getMethod("getHeaders");
            Object headersObj = getHeaders.invoke(req);
            Class<?> classOfDefaultHttpHeaders = headersObj.getClass();
            Method entries = classOfDefaultHttpHeaders.getMethod("entries");
            ArrayList<Object> headers = (ArrayList) entries.invoke(headersObj);
            Map<String,String> mapOfHeader = new HashMap<>();
            for (Object header:headers){
                String[] split = header.toString().split("=");
                String key = split[0];
                String value = split[1];
                mapOfHeader.put(key,value);
            }
            return mapOfHeader;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return new HashMap<String,String>();
        }


    }

    private static Map<String,String> getResHeader(Class<?> classOfDefaultWebReq, Object res) {
        try {
            Method getHeaders = classOfDefaultWebReq.getMethod("getHeaders");
            Object headersObj = getHeaders.invoke(res);
            Class<?> classOfDefaultHttpHeaders = headersObj.getClass();
            Method entries = classOfDefaultHttpHeaders.getMethod("entries");
            ArrayList<Object> headers = (ArrayList) entries.invoke(headersObj);
            Map<String,String> mapOfHeader = new HashMap<>();
            for (Object header:headers){
                String[] split = header.toString().split("=");
                String key = split[0];
                String value = split[1];
                mapOfHeader.put(key,value);
            }
            return mapOfHeader;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return new HashMap<String,String>();
        }
    }

    private static String getPath(Class<?> classOfDefaultWebReq, Object req) {
        try {
            Method getPath = classOfDefaultWebReq.getMethod("getPath");
            return String.valueOf(getPath.invoke(req));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return "";
        }
    }

    public static void solveKrpcApi(MethodEvent event, AtomicInteger invokeIdSequencer) {
        if (!EngineManager.KRPC_API_SITEMAP_IS_SEND) {
            List<Object> listOfWebUrl = (List<Object>) event.argumentArray[0];
            new KrpcApiThread(listOfWebUrl).start();
        }
    }

    private static String getUri(Object invocation) {
        try {
            Class<?> invocationClass = invocation.getClass();
            Method methodOfGetAttachments = invocationClass.getMethod("getMeta");
            Object RpcMeta = methodOfGetAttachments.invoke(invocation);
            Class<?> rpcMetaClass = RpcMeta.getClass();
            Method methodOfGetServiceId = rpcMetaClass.getMethod("getServiceId");
            String serviceId = String.valueOf(methodOfGetServiceId.invoke(RpcMeta));
            Method methodOfGetMsgId = rpcMetaClass.getMethod("getMsgId");
            String msgId = String.valueOf(methodOfGetMsgId.invoke(RpcMeta));
            KrpcApiModel krpcApiModel = EngineManager.KRPC_API_SITEMAP.get(serviceId + msgId);
            return krpcApiModel.getPath();
        } catch (Exception var14) {
            return "null";
        }
    }

    private static String getUrl(Object invocation) {
        try {
            Class<?> RpcMetaClass = invocation.getClass();
            Method connIdMethod = RpcMetaClass.getMethod("getConnId");
            return (String) connIdMethod.invoke(invocation);
        } catch (Exception e) {
            return "";
        }
    }

    private static final String VALUES_ENUMERATOR = " org.apache.tomcat.util.http.ValuesEnumerator".substring(1);
    private static final String SPRING_OBJECT = " org.springframework.".substring(1);

    public static void handlerCustomModel(MethodEvent event, Object argument) {
        try {
            Class<?> sourceClass = argument.getClass();
            if (sourceClass.getClassLoader() == null) {
                return;
            }
            String className = sourceClass.getName();
            if (className.startsWith("cn.huoxian.iast.api.") ||
                    className.startsWith("io.dongtai.api.") ||
                    VALUES_ENUMERATOR.equals(className) ||
                    className.startsWith(SPRING_OBJECT)
            ) {
                return;
            }
            Method[] methods = sourceClass.getDeclaredMethods();
            Object itemValue = null;
            for (Method method : methods) {
                String methodName = method.getName();
                if (!methodName.startsWith("get")
                        || methodName.equals("getClass")
                        || methodName.equals("getParserForType")
                        || methodName.equals("getDefaultInstance")
                        || methodName.equals("getDefaultInstanceForType")
                        || methodName.equals("getDescriptor")
                        || methodName.equals("getDescriptorForType")
                        || methodName.equals("getAllFields")
                        || methodName.equals("getInitializationErrorString")
                        || methodName.equals("getUnknownFields")
                        || methodName.equals("getDetailOrBuilderList")
                        || methodName.equals("getAllFieldsMutable")
                        || methodName.equals("getAllFieldsRaw")
                        || methodName.equals("getOneofFieldDescriptor")
                        || methodName.equals("getField")
                        || methodName.equals("getFieldRaw")
                        || methodName.equals("getRepeatedFieldCount")
                        || methodName.equals("getRepeatedField")
                        || methodName.equals("getSerializedSize")
                        || methodName.equals("getMethodOrDie")
                        || methodName.endsWith("Bytes")
                        || method.getParameterCount() != 0) {
                    continue;
                }

                Class<?> returnType = method.getReturnType();
                if (returnType == Integer.class ||
                        returnType == Boolean.class ||
                        returnType == Long.class ||
                        returnType == Character.class ||
                        returnType == Double.class ||
                        returnType == Float.class ||
                        returnType == Enum.class ||
                        returnType == Byte.class ||
                        returnType == int.class ||
                        returnType == boolean.class ||
                        returnType == long.class ||
                        returnType == char.class ||
                        returnType == double.class ||
                        returnType == float.class ||
                        returnType == byte.class
                ) {
                    continue;
                }

                itemValue = method.invoke(argument);
                if (!isNotEmpty(itemValue)) {
                    continue;
                }
                addTaintToPool(itemValue, event, true);
            }

        } catch (Throwable t) {
            DongTaiLog.error(t);
        }
    }

    public static void addTaintToPool(Object obj, MethodEvent event, boolean isSource) {
        int subHashCode = 0;
        Set<Object> taintPool = EngineManager.TAINT_POOL.get();
        if (obj instanceof String[]) {
            taintPool.add(obj);
            event.addTargetHash(obj.hashCode());

            String[] tempObjs = (String[]) obj;
            if (PROPERTIES.isNormalMode()) {
                for (String tempObj : tempObjs) {
                    EngineManager.TAINT_POOL.get().add(tempObj);
                    subHashCode = System.identityHashCode(tempObj);
                    EngineManager.TAINT_HASH_CODES.get().add(subHashCode);
                    event.addTargetHash(subHashCode);
                }
            } else {
                for (String tempObj : tempObjs) {
                    EngineManager.TAINT_POOL.get().add(tempObj);
                    event.addTargetHash(tempObj.hashCode());
                }
            }
        } else if (obj instanceof Map) {
            EngineManager.TAINT_POOL.get().add(obj);
            event.addTargetHash(obj.hashCode());
            if (isSource) {
                Map<String, String[]> tempMap = (Map<String, String[]>) obj;
                Set<Map.Entry<String, String[]>> entries = tempMap.entrySet();
                for (Map.Entry<String, String[]> entry : entries) {
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    addTaintToPool(key, event, true);
                    addTaintToPool(value, event, true);
                }
            }
        } else if (obj.getClass().isArray() && !obj.getClass().getComponentType().isPrimitive()) {
            Object[] tempObjs = (Object[]) obj;
            if (tempObjs.length != 0) {
                for (Object tempObj : tempObjs) {
                    addTaintToPool(tempObj, event, isSource);
                }
            }
        } else {
            taintPool.add(obj);
            Set<Integer> taintHashCodes = EngineManager.TAINT_HASH_CODES.get();
            if (obj instanceof String && PROPERTIES.isNormalMode()) {
                subHashCode = System.identityHashCode(obj);
                taintHashCodes.add(subHashCode);
            } else {
                subHashCode = obj.hashCode();
            }
            event.addTargetHash(subHashCode);

        }
    }

    /**
     * get Dubbo Attachments
     *
     * @param invocation object of Invocation
     * @return Map<String, String>
     * @since 1.2.0
     */
    public static Map<String, String> getAttachments(Object invocation) {
        try {
            Class<?> invocationClass = invocation.getClass();
            Method methodOfGetAttachments = invocationClass.getMethod("getMeta");
            Object RpcMeta = methodOfGetAttachments.invoke(invocation);
            Class<?> RpcMetaClass = RpcMeta.getClass();
            Map<String, String> RpcMetaMap = new HashMap<>();

            Method getTrace = RpcMetaClass.getMethod("getTrace");
            String traceId = String.valueOf(getTrace.invoke(RpcMeta));
            String[] traceIdSplit = traceId.split("\n");
            for (String s : traceIdSplit) {
                String[] split = s.split(":");
                RpcMetaMap.put(split[0], split[1]);
            }


            return RpcMetaMap;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * get Dubbo Arguments
     *
     * @param invocation object of Invocation
     * @return Object[]
     * @since 1.2.0
     */
    public static Object getArguments(Object invocation) {
        try {
            Class<?> invocationClass = invocation.getClass();
            Method methodOfGetAttachments = invocationClass.getMethod("getBody");
            return methodOfGetAttachments.invoke(invocation);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isAllowTaintType(Object obj) {
        return !(obj instanceof Boolean || obj instanceof Integer);
    }

    /**
     * 检查对象是否为空 - 集合类型，检查大小 - 字符串类型，检查是否为空字符串 - 其他情况，均认为非空
     *
     * @param obj 待检查的实例化对象
     * @return true-对象不为空；false-对象为空
     */
    private static boolean isNotEmpty(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Map) {
            Map<?, ?> taintValue = (Map<?, ?>) obj;
            return !taintValue.isEmpty();
        } else if (obj instanceof List) {
            List<?> taintValue = (List<?>) obj;
            return !taintValue.isEmpty();
        } else if (obj instanceof Set) {
            Set<?> taintValue = (Set<?>) obj;
            return !taintValue.isEmpty();
        } else if (obj instanceof String) {
            String taintValue = (String) obj;
            return !taintValue.isEmpty();
        }
        return true;
    }
}
