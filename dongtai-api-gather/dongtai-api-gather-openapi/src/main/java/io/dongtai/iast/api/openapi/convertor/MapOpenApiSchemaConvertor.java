package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.DataType;
import io.dongtai.iast.api.openapi.domain.Schema;

/**
 * 用于把Map类型转为Open API的类型
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class MapOpenApiSchemaConvertor extends BaseOpenApiSchemaConvertor {

    public MapOpenApiSchemaConvertor(OpenApiSchemaConvertorManager manager) {
        super(manager);
    }

    @Override
    public String getConvertorName() {
        return "map-convertor";
    }

    @Override
    public boolean canConvert(Class clazz) {
        return false;
    }

    @Override
    public Schema convert(Class clazz) {
        // 2023-6-30 10:39:26 暂不处理map
        return new Schema(DataType.Object());
    }

}
