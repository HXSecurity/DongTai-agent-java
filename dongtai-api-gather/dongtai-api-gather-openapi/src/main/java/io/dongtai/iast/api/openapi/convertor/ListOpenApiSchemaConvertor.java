package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.DataType;
import io.dongtai.iast.api.openapi.domain.Schema;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 处理List结构
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class ListOpenApiSchemaConvertor extends BaseOpenApiSchemaConvertor {

    public ListOpenApiSchemaConvertor(OpenApiSchemaConvertorManager manager) {
        super(manager);
    }

    @Override
    public String getConvertorName() {
        return "list-convertor";
    }

    @Override
    public boolean canConvert(Class clazz) {
        return clazz != null && List.class.isAssignableFrom(clazz);
    }

    @Override
    public Schema convert(Class clazz) {
        return new Schema(DataType.ObjectArray());
    }

    @Override
    public Schema convert(Class clazz, Field field) {
        Schema itemsComponent = convertField(clazz, field);
        return new Schema(DataType.Array(itemsComponent));
    }


    private Schema convertField(Class clazz, Field field) {
//        if (field == null || clazz == null) {
//            return null;
//        }
//        Type genericType = field.getGenericType();
//        if (genericType == null || !(genericType instanceof ParameterizedType)) {
//            return null;
//        }
//        ParameterizedType pt = (ParameterizedType) genericType;
//        Type[] actualTypeArguments = pt.getActualTypeArguments();
//        if (actualTypeArguments == null || actualTypeArguments.length == 0) {
//            return null;
//        }
//        Class actualTypeArgument = ((ParameterizedTypeImpl) actualTypeArguments[0]).getRawType();
//        return manager.convertClass(actualTypeArgument);

        // 2023-6-19 11:13:45 当前版本暂不处理泛型，直接认为是object
        return new Schema(DataType.Object());
    }

}
