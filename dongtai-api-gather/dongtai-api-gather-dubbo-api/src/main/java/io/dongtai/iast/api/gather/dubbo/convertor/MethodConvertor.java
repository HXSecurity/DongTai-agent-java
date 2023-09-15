package io.dongtai.iast.api.gather.dubbo.convertor;

import io.dongtai.iast.api.openapi.convertor.OpenApiSchemaConvertorManager;
import io.dongtai.iast.api.openapi.domain.MediaType;
import io.dongtai.iast.api.openapi.domain.Operation;
import io.dongtai.iast.api.openapi.domain.Parameter;
import io.dongtai.iast.api.openapi.domain.Response;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 用于把Dubbo的Service的方法转为Open API的Operation结构
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class MethodConvertor {

    private final OpenApiSchemaConvertorManager manager;
    private final Method reflectionMethod;

    /**
     * @param manager
     * @param reflectionMethod 要转换的Method，一个Method对应着一个Operation
     */
    public MethodConvertor(OpenApiSchemaConvertorManager manager, Method reflectionMethod) {
        this.manager = manager;
        this.reflectionMethod = reflectionMethod;
    }

    public Operation convert() {
        Operation o = new Operation();

        try {
            o.mergeParameters(this.parseParameters());
        } catch (Throwable e) {
            DongTaiLog.debug("MethodConvertor.convert parseParameters exception", e);
        }

        try {
            o.setResponses(this.parseResponse());
        } catch (Throwable e) {
            DongTaiLog.debug("MethodConvertor.convert parseResponse exception", e);
        }

        // 设置这两个字段
        o.setOperationId(UUID.randomUUID().toString());
        // 把类名设置为标签
        o.setTags(Collections.singletonList(reflectionMethod.getDeclaringClass().getName()));

        return o;
    }

    /**
     * 把Dubbo的Service的方法返回值转换为Open API的Response
     *
     * @return
     */
    private Map<String, Response> parseResponse() {

        Class<?> returnType = this.reflectionMethod.getReturnType();
        // 这里需要注意，可能会有返回值为空的情况，这种情况就认为是没有响应值
        if (Void.TYPE == returnType) {
            return null;
        }

        // 把函数的返回值对应到HTTP的响应体上
        Response r = new Response();
        Map<String, MediaType> contentMap = new HashMap<>();
        MediaType mediaType = new MediaType();
        mediaType.setSchema(this.manager.convertClass(returnType));
        contentMap.put(MediaType.APPLICATION_JSON, mediaType);
        r.setContent(contentMap);

        // 这里只处理正常返回的情况，认为是200的情况，至于throws抛出异常500的情况就不再处理了
        Map<String, Response> responseMap = new HashMap<>();
        r.setDescription(Response.MSG_OK);
        responseMap.put(Response.CODE_OK, r);

        return responseMap;
    }

    /**
     * 解析Method上的参数为OpenAPI的Parameter
     *
     * @return
     */
    private List<Parameter> parseParameters() {
        java.lang.reflect.Parameter[] reflectionParameterArray = this.reflectionMethod.getParameters();
        if (reflectionParameterArray == null || reflectionParameterArray.length == 0) {
            return Collections.emptyList();
        }
        List<Parameter> parameterList = new ArrayList<>();
        for (java.lang.reflect.Parameter reflectionParameter : reflectionParameterArray) {
            try {
                Parameter convert = new ParameterConvertor(this.manager, reflectionParameter).convert();
                if (convert != null) {
                    parameterList.add(convert);
                }
            } catch (Throwable e) {
                DongTaiLog.debug("MethodConvertor.parseParameters ParameterConvertor exception", e);
            }
        }
        return parameterList;
    }

}
