package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.Schema;

import java.lang.reflect.Field;

/**
 * 可以根据Java中的Field转换为Open API的Schema
 *
 * @author CC11001100
 * @since v1.12.0
 */
public interface FieldOpenApiSchemaConvertor {

    /**
     * 转换器的名字，方便日志打印啥的
     *
     * @return
     */
    String getConvertorName();

    /**
     * 判断是否能够转换Field
     *
     * @param clazz
     * @param field
     * @return
     */
    boolean canConvert(Class clazz, Field field);

    /**
     * 实际进行转换，把Field转换为一个组件
     *
     * @param clazz
     * @param field
     * @return 如果转换失败的话返回null
     */
    Schema convert(Class clazz, Field field);

}
