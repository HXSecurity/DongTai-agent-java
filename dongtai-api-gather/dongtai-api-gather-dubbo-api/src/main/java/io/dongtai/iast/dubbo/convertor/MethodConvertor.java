package io.dongtai.iast.dubbo.convertor;

import io.dongtai.iast.openapi.convertor.OpenApiSchemaConvertorManager;
import io.dongtai.iast.openapi.domain.MediaType;
import io.dongtai.iast.openapi.domain.Operation;
import io.dongtai.iast.openapi.domain.Parameter;
import io.dongtai.iast.openapi.domain.Response;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 用于把Dubbo的Service的方法转为Open API的Operation结构
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class MethodConvertor {

    private OpenApiSchemaConvertorManager manager;
    private Method reflectionMethod;

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

        o.mergeParameters(this.parseParameters());
        o.setResponses(this.parseResponse());

        // TODO 2023-6-26 11:24:05 设置这两个字段
//        o.setOperationId();
//        o.setTags();

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
        // TODO 2023-6-26 11:25:24 需要确认open api的协议是否支持响应为空
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
            Parameter convert = new ParameterConvertor(this.manager, reflectionParameter).convert();
            if (convert != null) {
                parameterList.add(convert);
            }
        }
        return parameterList;
    }

}
