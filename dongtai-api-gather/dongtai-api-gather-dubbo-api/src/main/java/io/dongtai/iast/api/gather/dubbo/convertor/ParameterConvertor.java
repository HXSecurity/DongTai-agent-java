package io.dongtai.iast.api.gather.dubbo.convertor;

import io.dongtai.iast.api.openapi.convertor.OpenApiSchemaConvertorManager;
import io.dongtai.iast.api.openapi.domain.Parameter;
import io.dongtai.iast.api.openapi.domain.ParameterIn;
import io.dongtai.iast.api.openapi.domain.Schema;

/**
 * 方法参数级别的转换，把Dubbo的Service上的Method的Parameter转为Open API的Parameter的格式
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class ParameterConvertor {

    private final OpenApiSchemaConvertorManager manager;
    private final java.lang.reflect.Parameter reflectionParameter;

    /**
     * @param manager
     * @param reflectionParameter 要转换的方法参数
     */
    public ParameterConvertor(OpenApiSchemaConvertorManager manager, java.lang.reflect.Parameter reflectionParameter) {
        this.manager = manager;
        this.reflectionParameter = reflectionParameter;
    }

    public Parameter convert() {

        Parameter openApiParameter = new Parameter();

        // 2023-6-25 18:23:17 以后得空的时候也许可以把这里优化一下，用asm拿到真正的参数名字，这样前端页面上用户看着心情会好一些
        openApiParameter.setName(reflectionParameter.getName());

        // 洞态开发人员内部约定：dubbo的参数固定认为是放在query上的，同时是必传的
        openApiParameter.setIn(ParameterIn.Query);
        openApiParameter.setRequired(true);

        // 参数的类型转为Open API的类型，如果有涉及到复合类型的话存储到Open API的组件库中
        Schema schema = this.manager.convertClass(this.reflectionParameter.getType());
        openApiParameter.setSchema(schema);

        return openApiParameter;
    }

}
