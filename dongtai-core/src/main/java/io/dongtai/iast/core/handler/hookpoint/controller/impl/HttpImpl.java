package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.common.config.*;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.IastClassLoader;
import io.dongtai.iast.core.utils.*;
import io.dongtai.iast.core.utils.matcher.ConfigMatcher;
import io.dongtai.log.DongTaiLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * Http方法处理入口
 *
 * @author dongzhiyong@huoxian.cn
 */
public class HttpImpl {
    private static IastClassLoader iastClassLoader;
    public static File IAST_REQUEST_JAR_PACKAGE;
    public static final String HEADER_DAST_MARK = "dt-mark-header";
    public static final String HEADER_DAST = "dt-dast";

    static {
        IAST_REQUEST_JAR_PACKAGE = new File(PropertyUtils.getTmpDir() + "dongtai-api.jar");
        if (!IAST_REQUEST_JAR_PACKAGE.exists()) {
            HttpClientUtils.downloadRemoteJar("/api/v1/engine/download?engineName=dongtai-api", IAST_REQUEST_JAR_PACKAGE.getAbsolutePath());
        }
    }


    public static void createClassLoader(Object req) {
        try {
            if (iastClassLoader != null) {
                return;
            }
            if (IAST_REQUEST_JAR_PACKAGE.exists()) {
                iastClassLoader = new IastClassLoader(
                        req.getClass().getClassLoader(),
                        new URL[]{IAST_REQUEST_JAR_PACKAGE.toURI().toURL()}
                );
            }
        } catch (Throwable e) {
            DongTaiLog.warn("HttpImpl createClassLoader failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void solveHttpRequest(Object obj, Object req, Object resp, Map<String, Object> requestMeta) {
        if (requestMeta == null || requestMeta.size() == 0) {
            return;
        }

        try {
            Config<RequestDenyList> config = (Config<RequestDenyList>) ConfigBuilder.getInstance()
                    .getConfig(ConfigKey.REQUEST_DENY_LIST);
            RequestDenyList requestDenyList = config.get();
            if (requestDenyList != null) {
                String requestURL = ((StringBuffer) requestMeta.get("requestURL")).toString();
                Map<String, String> headers = (Map<String, String>) requestMeta.get("headers");
                if (requestDenyList.match(requestURL, headers)) {
                    DongTaiLog.trace("HTTP Request {} deny to collect", requestURL);
                    return;
                }
            }
        } catch (Throwable ignore) {
        }

        Boolean isReplay = (Boolean) requestMeta.get("replay-request");
        if (isReplay) {
            EngineManager.ENTER_REPLAY_ENTRYPOINT.enterEntry();
        }
        // todo Consider increasing the capture of html request responses
        if (ConfigMatcher.getInstance().disableExtension((String) requestMeta.get("requestURI"))) {
            return;
        }
        if (ConfigMatcher.getInstance().getBlackUrl(requestMeta)) {
            return;
        }

        try {
            boolean enableVersionHeader = ((Config<Boolean>) ConfigBuilder.getInstance()
                    .getConfig(ConfigKey.ENABLE_VERSION_HEADER)).get();
            String dastHeader = ((Map<String, String>) requestMeta.get("headers")).get(HEADER_DAST);
            String dastMarkHeader = ((Map<String, String>) requestMeta.get("headers")).get(HEADER_DAST_MARK);
            if (enableVersionHeader || dastHeader != null || dastMarkHeader != null) {
                Method setHeaderMethod = ReflectUtils.getDeclaredMethodFromSuperClass(resp.getClass(),
                        "setHeader", new Class[]{String.class, String.class});
                if (setHeaderMethod != null) {
                    if (enableVersionHeader) {
                        String versionHeaderKey = ((Config<String>) ConfigBuilder.getInstance()
                                .getConfig(ConfigKey.VERSION_HEADER_KEY)).get();
                        setHeaderMethod.invoke(resp, versionHeaderKey, AgentConstant.VERSION_VALUE);
                    }
                    if (dastMarkHeader != null) {
                        String reqId = String.valueOf(EngineManager.getAgentId()) + "."
                                + UUID.randomUUID().toString().replaceAll("-", "");
                        setHeaderMethod.invoke(resp, "dt-request-id", reqId);
                    }
                }
                if (dastHeader != null) {
                    return;
                }
            }
        } catch (Throwable ignore) {
        }

        try {
            String contentType = ((Map<String, String>) requestMeta.get("headers")).get("Content-Type");
            String method = (String) requestMeta.get("method");
            if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method))
                    && !isRawBody(contentType)) {
                Method getParameterNamesMethod = ReflectUtils.getDeclaredMethodFromSuperClass(req.getClass(),
                        "getParameterNames", null);
                Method getParameterMethod = ReflectUtils.getDeclaredMethodFromSuperClass(req.getClass(),
                        "getParameter", new Class[]{String.class});
                Enumeration<?> parameterNames = (Enumeration<?>) getParameterNamesMethod.invoke(req);
                StringBuilder postBody = new StringBuilder();
                boolean first = true;
                while (parameterNames.hasMoreElements()) {
                    String key = (String) parameterNames.nextElement();
                    if (first) {
                        first = false;
                        postBody.append(key).append("=").append((String) getParameterMethod.invoke(req, key));
                    } else {
                        postBody.append("&").append(key).append("=").append((String) getParameterMethod.invoke(req, key));
                    }
                }
                if (postBody.length() > 0) {
                    requestMeta.put("body", postBody.toString());
                }
            }
        } catch (Throwable ignore) {
        }

        EngineManager.enterHttpEntry(requestMeta);
        DongTaiLog.debug("HTTP Request:{} {} from: {}", requestMeta.get("method"), requestMeta.get("requestURI"),
                obj.getClass().getName());
    }

    public static Map<String, String> parseRequestHeaders(Object req, Enumeration<?> headerNames) {
        Map<String, String> headers = new HashMap<String, String>(32);
        Method getHeaderMethod = ReflectUtils.getDeclaredMethodFromSuperClass(req.getClass(),
                "getHeader", new Class[]{String.class});
        if (getHeaderMethod == null) {
            return headers;
        }
        while (headerNames.hasMoreElements()) {
            try {
                String key = (String) headerNames.nextElement();
                String val = (String) getHeaderMethod.invoke(req, key);
                if ("content-type".equalsIgnoreCase(key)) {
                    key = "Content-Type";
                } else if (HEADER_DAST_MARK.equalsIgnoreCase(key)) {
                    key = HEADER_DAST_MARK;
                } else if (HEADER_DAST.equalsIgnoreCase(key)) {
                    key = HEADER_DAST;
                }
                headers.put(key, val);
            } catch (Throwable ignore) {
            }
        }
        return headers;
    }

    public static void onServletInputStreamRead(int ret, String desc, Object stream, byte[] bs, int offset, int len) {
        if (EngineManager.REQUEST_CONTEXT.get() != null
                && EngineManager.REQUEST_CONTEXT.get().get("body") != null
                && EngineManager.REQUEST_CONTEXT.get().get("body") != ""
        ) {
            return;
        }

        if ("()I".equals(desc)) {
            if (ret == -1) {
                return;
            }
            ByteArrayOutputStream buff = EngineManager.BODY_BUFFER.getRequest();
            if (buff.size() < 4096) {
                buff.write(ret);
            }
        } else if ("([B)I".equals(desc)) {
            if (ret == -1 || bs == null) {
                return;
            }
            onServletInputStreamRead(ret, "([BII)I", stream, bs, 0, bs.length);
        } else if ("([BII)I".equals(desc)) {
            if (bs == null || offset < 0 || len < 0 || ret == -1) {
                return;
            }

            ByteArrayOutputStream buff = EngineManager.BODY_BUFFER.getRequest();
            int size = buff.size();
            if (size < 4096) {
                buff.write(bs, offset, Math.min(ret, 4096 - size));
            }
        }
    }

    public static void solveHttpResponse(Object obj, Object req, Object resp, Collection<?> headerNames, int status) {
        if (EngineManager.REQUEST_CONTEXT.get() == null) {
            return;
        }
        Map<String, Collection<String>> headers = parseResponseHeaders(resp, headerNames);
        EngineManager.REQUEST_CONTEXT.get().put("responseStatus",
                (String) EngineManager.REQUEST_CONTEXT.get().get("protocol") + " " + status);
        EngineManager.REQUEST_CONTEXT.get().put("responseHeaders", headers);
    }

    public static Map<String, Collection<String>> parseResponseHeaders(Object resp, Collection<?> headerNames) {
        Map<String, Collection<String>> headers = new HashMap<String, Collection<String>>(32);
        Method getHeadersMethod = ReflectUtils.getDeclaredMethodFromSuperClass(resp.getClass(),
                "getHeaders", new Class[]{String.class});
        if (getHeadersMethod == null) {
            return headers;
        }
        for (Object key : headerNames) {
            try {
                Collection<String> val = (Collection<String>) getHeadersMethod.invoke(resp, key);
                headers.put((String) key, val);
            } catch (Throwable ignore) {
            }
        }
        return headers;
    }

    public static void onServletOutputStreamWrite(String desc, Object stream, int b, byte[] bs, int offset, int len) {
        try {
            boolean getBody = ((Config<Boolean>) ConfigBuilder.getInstance().getConfig(ConfigKey.REPORT_RESPONSE_BODY)).get();
            if (!getBody) {
                return;
            }
        } catch (Throwable ignore) {
            return;
        }

        Integer maxLength = PropertyUtils.getInstance().getResponseLength();
        if (maxLength == 0) {
            return;
        } else if (maxLength < 0 || maxLength > 50000) {
            maxLength = 50000;
        }

        try {
            if ("(I)V".equals(desc)) {
                if (b == -1) {
                    return;
                }
                ByteArrayOutputStream buff = EngineManager.BODY_BUFFER.getResponse();
                if (buff.size() < maxLength) {
                    buff.write(b);
                }
            } else if ("([B)V".equals(desc)) {
                if (bs == null) {
                    return;
                }
                onServletOutputStreamWrite("([BII)V", stream, b, bs, 0, bs.length);
            } else if ("([BII)V".equals(desc)) {
                if (bs == null || offset < 0 || len < 0) {
                    return;
                }

                ByteArrayOutputStream buff = EngineManager.BODY_BUFFER.getResponse();
                int size = buff.size();
                if (size < maxLength) {
                    buff.write(bs, offset, Math.min(len, maxLength - size));
                }
            }
        } catch (Throwable ignore) {
        }
    }

    public static void onPrintWriterWrite(String desc, Object writer, int b, String s, char[] cs, int offset, int len) {
        try {
            boolean getBody = ((Config<Boolean>) ConfigBuilder.getInstance().getConfig(ConfigKey.REPORT_RESPONSE_BODY)).get();
            if (!getBody) {
                return;
            }
        } catch (Throwable ignore) {
            return;
        }

        Integer maxLength = PropertyUtils.getInstance().getResponseLength();
        if (maxLength == 0) {
            return;
        } else if (maxLength < 0 || maxLength > 50000) {
            maxLength = 50000;
        }

        try {
            if ("(I)V".equals(desc)) {
                if (b == -1) {
                    return;
                }
                ByteArrayOutputStream buff = EngineManager.BODY_BUFFER.getResponse();
                if (buff.size() < maxLength) {
                    buff.write(b);
                }
            } else if ("([CII)V".equals(desc)) {
                if (cs == null || offset < 0 || len < 0) {
                    return;
                }

                ByteArrayOutputStream buff = EngineManager.BODY_BUFFER.getResponse();
                int size = buff.size();
                if (size < maxLength) {
                    buff.write((new String(cs, offset, Math.min(len, maxLength - size))).getBytes());
                }
            } else if ("(Ljava/lang/String;II)V".equals(desc)) {
                if (StringUtils.isEmpty(s) || offset < 0 || len < 0) {
                    return;
                }

                ByteArrayOutputStream buff = EngineManager.BODY_BUFFER.getResponse();
                int size = buff.size();
                if (size < maxLength) {
                    buff.write((new String(s.toCharArray(), offset, Math.min(len, maxLength - size))).getBytes());
                }
            }
        } catch (Throwable ignore) {
        }
    }

    public static IastClassLoader getClassLoader() {
        return iastClassLoader;
    }

    public static boolean isRawBody(String contentType) {
        return contentType != null
                && (contentType.contains("application/json") || contentType.contains("application/xml"));
    }
}
