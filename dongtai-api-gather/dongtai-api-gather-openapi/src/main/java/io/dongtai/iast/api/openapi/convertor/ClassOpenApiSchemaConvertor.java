package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.Schema;

/**
 * 用于表示一个类型转换器，用来根据Java中的Class转换为Open API的Schema
 *
 * @author CC11001100
 * @since v1.12.0
 */
public interface ClassOpenApiSchemaConvertor {

    /**
     * 转换器的名字，方便日志打印啥的
     *
     * @return
     */
    String getConvertorName();

    /**
     * 判断是否能够转换
     *
     * @param clazz
     * @return
     */
    boolean canConvert(Class clazz);

    /**
     * 实际进行转换
     *
     * @param clazz
     * @return 如果转换失败的话返回null
     */
    Schema convert(Class clazz);

}
