package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.DataType;
import io.dongtai.iast.api.openapi.domain.Schema;

/**
 * 转换枚举值
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class EnumOpenApiSchemaConvertor extends BaseOpenApiSchemaConvertor {

    public EnumOpenApiSchemaConvertor(OpenApiSchemaConvertorManager manager) {
        super(manager);
    }

    @Override
    public String getConvertorName() {
        return "enum-convertor";
    }

    @Override
    public boolean canConvert(Class clazz) {
        return clazz != null && clazz.isEnum();
    }

    @Override
    public Schema convert(Class clazz) {

        if (clazz == null) {
            return null;
        }

        // Open API里枚举类型是被看做一个有限取值的string
        Schema schema = manager.database.find(clazz);
        if (schema != null) {
            return schema;
        }

        // 处理枚举值
        Object[] enumConstants = clazz.getEnumConstants();
        String[] enums = new String[enumConstants.length];
        for (int i = 0; i < enumConstants.length; i++) {
            enums[i] = enumConstants[i].toString();
        }

        Schema s = new Schema(DataType.String());
        s.setEnums(enums);

        // 枚举不会有dfs，所以不需要触发转换完成的回调

        return s;
    }

}
