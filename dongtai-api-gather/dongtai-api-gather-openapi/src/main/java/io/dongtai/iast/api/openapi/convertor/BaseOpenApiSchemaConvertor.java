package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.Schema;

import java.lang.reflect.Field;

/**
 * 用于对Convertor的共性做约束，比如所有的Convertor都需要manager，创建的时候必须注入
 *
 * @author CC11001100
 * @since v1.12.0
 */
public abstract class BaseOpenApiSchemaConvertor implements ClassOpenApiSchemaConvertor, FieldOpenApiSchemaConvertor {

    protected OpenApiSchemaConvertorManager manager;

    public BaseOpenApiSchemaConvertor(OpenApiSchemaConvertorManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean canConvert(Class clazz, Field field) {
        // 在基类上把Field的转换复用一下Class的转换 
        return field != null && canConvert(field.getType());
    }

    @Override
    public Schema convert(Class clazz, Field field) {
        if (field == null) {
            return null;
        }
        return convert(field.getType());
    }

}
