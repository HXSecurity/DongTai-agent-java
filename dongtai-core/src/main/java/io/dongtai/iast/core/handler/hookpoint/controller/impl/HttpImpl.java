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
            if (enableVersionHeader) {
                String versionHeaderKey = ((Config<String>) ConfigBuilder.getInstance()
                        .getConfig(ConfigKey.VERSION_HEADER_KEY)).get();
                Method setHeaderMethod = ReflectUtils.getDeclaredMethodFromSuperClass(resp.getClass(),
                        "setHeader", new Class[]{String.class, String.class});
                if (setHeaderMethod != null) {
                    setHeaderMethod.invoke(resp, versionHeaderKey, AgentConstant.VERSION_VALUE);
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
                headers.put(key, val);
            } catch (Throwable ignore) {
            }
        }
        return headers;
    }

    public static void onServletInputStreamRead(int ret, String desc, Object stream, byte[] bs, int offset, int len) {
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
    }

    public static IastClassLoader getClassLoader() {
        return iastClassLoader;
    }
}
