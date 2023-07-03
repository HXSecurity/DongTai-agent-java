package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.DataType;
import io.dongtai.iast.api.openapi.domain.Schema;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * 处理Set类型
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class SetOpenApiSchemaConvertor extends BaseOpenApiSchemaConvertor {

    public SetOpenApiSchemaConvertor(OpenApiSchemaConvertorManager manager) {
        super(manager);
    }

    @Override
    public String getConvertorName() {
        return "set-convertor";
    }

    @Override
    public boolean canConvert(Class clazz) {
        return clazz != null && Set.class.isAssignableFrom(clazz);
    }

    @Override
    public Schema convert(Class clazz) {
        // 2023-6-19 11:25:46 暂不处理泛型参数
        Schema c = new Schema(DataType.ObjectArray());
        c.setUniqueItems(true);
        return c;
    }

    @Override
    public Schema convert(Class clazz, Field field) {
        return this.convert(field.getClass());
    }
}
