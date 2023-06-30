package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.DataType;
import io.dongtai.iast.api.openapi.domain.Schema;

/**
 * 用于转换Java的数组结构到OpenApi的array组件类型
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class ArrayOpenApiSchemaConvertor extends BaseOpenApiSchemaConvertor {

    public ArrayOpenApiSchemaConvertor(OpenApiSchemaConvertorManager manager) {
        super(manager);
    }

    @Override
    public String getConvertorName() {
        return "array-convertor";
    }

    @Override
    public boolean canConvert(Class clazz) {
        return clazz != null && clazz.isArray();
    }

    @Override
    public Schema convert(Class clazz) {

        Class componentType = clazz.getComponentType();
        if (componentType == null) {
            return new Schema(DataType.ObjectArray());
        }

        // 如果是多层数组，则直接返回array，swagger spring也是这么处理的
        if (componentType.isArray()) {
            return new Schema(DataType.ObjectArray());
        }

        // 尝试解析数组的items的组件类型
        Schema itemsComponent = manager.convertClass(componentType);
        if (itemsComponent == null) {
            return new Schema(DataType.ObjectArray());
        } else {
            return new Schema(DataType.Array(itemsComponent));
        }
    }

}
