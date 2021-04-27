package com.secnium.iast.core.handler.controller.impl;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.handler.vulscan.overpower.LoginLogicRecognize;
import com.secnium.iast.core.middlewarerecognition.IastServer;
import com.secnium.iast.core.util.http.HttpRequest;
import com.secnium.iast.core.util.http.HttpResponse;
import com.secnium.iast.core.util.matcher.ConfigMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Http方法处理入口
 *
 * @author dongzhiyong@huoxian.cn
 */
public class HttpImpl {
    /**
     * 处理http请求
     *
     * @param event 待处理的方法调用事件
     */
    public static void solveHttp(MethodEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug(EngineManager.SCOPE_TRACKER.get().toString());
        }
        HttpRequest request = new HttpRequest(event.argumentArray[0]);

        // todo：测试使用正则是否可提升效率
        if (!ConfigMatcher.disableExtention(request.getRequestURI())) {

            if (LoginLogicRecognize.isLoginUrl(request.getRequestURI())) {
                EngineManager.setIsLoginLogic();
            }

            if (null == EngineManager.SERVER) {
                EngineManager.SERVER = new IastServer(request.getServerName(), request.getServerPort(), true);
            }
            EngineManager.ENTER_HTTP_ENTRYPOINT.enterHttpEntryPoint();
            EngineManager.REQUEST_CONTEXT.set(request);
            EngineManager.RESPONSE_CACHE.set(new HttpResponse(event.argumentArray[1]));
            EngineManager.TRACK_MAP.set(new HashMap<Integer, MethodEvent>());
            EngineManager.TAINT_POOL.set(new HashSet<Object>());
            EngineManager.TAINT_HASH_CODES.set(new HashSet<Integer>());

            if (logger.isDebugEnabled()) {
                logger.debug("HTTP Request:{} {} from: {}", request.getMethod(), request.getRequestURL(), event.signature);
            }
        }
    }

    private static final Logger logger = com.secnium.iast.core.AgentEngine.DEFAULT_LOGGERCONTEXT.getLogger(HttpImpl.class);
}
