package io.dongtai.iast.api.gather.spring.convertor;

import io.dongtai.iast.api.openapi.convertor.OpenApiSchemaConvertorManager;
import io.dongtai.iast.api.openapi.domain.*;
import io.dongtai.log.DongTaiLog;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析负责接收处理请求的Handler方法
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class HandlerMethodConvertor {

    private final OpenApiSchemaConvertorManager manager;
    private final WebApplicationContext webApplicationContext;
    private final Operation operation;
    private final HandlerMethod handlerMethod;

    public HandlerMethodConvertor(OpenApiSchemaConvertorManager manager, WebApplicationContext webApplicationContext, Operation operation, HandlerMethod handlerMethod) {
        this.manager = manager;
        this.webApplicationContext = webApplicationContext;
        this.operation = operation;
        this.handlerMethod = handlerMethod;
    }

    public void parse() {

        // 请求参数
        List<Parameter> parameterList = parseParameters();
        this.operation.mergeParameters(parameterList);

        // 响应
        Map<String, Response> responseMap = parseResponse();
        this.operation.setResponses(responseMap);
    }

    /**
     * 解析接口的参数
     *
     * @return
     */
    private List<Parameter> parseParameters() {
        Map<String, Parameter> parameterMap = new HashMap<>();
        MethodParameter[] methodParameters = this.handlerMethod.getMethodParameters();
        for (MethodParameter methodParameter : methodParameters) {
            try {
                Parameter p = new MethodParameterConvertor(this.manager, this.webApplicationContext, this.operation, methodParameter).parse();
                if (p != null) {
                    parameterMap.put(p.getName(), p);
                }
            } catch (Throwable e) {
                DongTaiLog.debug("spring HandlerMethodConvertor.parseParameters exception", e);
            }
        }
        return new ArrayList<>(parameterMap.values());
    }

    /**
     * 解析接口的响应类型
     *
     * @return
     */
    private Map<String, Response> parseResponse() {
        Response r = new Response();

        MediaType mediaType = new MediaType();
        Schema schema = this.manager.convertClass(this.handlerMethod.getReturnType().getParameterType());
        mediaType.setSchema(schema);

        // 如果响应类型是引用类型的，则认为它返回的是json，否则就认为是*/*
        Map<String, MediaType> contentMap = new HashMap<>();
        if (schema != null && schema.canRef()) {
            contentMap.put(MediaType.APPLICATION_JSON, mediaType);
        } else {
            contentMap.put(MediaType.ALL, mediaType);
        }

        r.setContent(contentMap);
        r.setDescription(Response.MSG_OK);

        Map<String, Response> responseMap = new HashMap<>();
        responseMap.put(Response.CODE_OK, r);

        return responseMap;
    }

}
