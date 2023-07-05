package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.DataType;
import io.dongtai.iast.api.openapi.domain.Schema;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 用于原生类型转换
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class PrimitiveTypeConvertor extends BaseOpenApiSchemaConvertor {

    // 原生类型，原生类型不需要存储到组件列表中
    public static Set<Class> primitiveTypeSet = new HashSet<>();

    static {

        // 这里把String也看做是primitive的了 ，虽然看起来可能有点奇怪...
        primitiveTypeSet.add(String.class);
        primitiveTypeSet.add(Date.class);

        primitiveTypeSet.add(Character.class);
        primitiveTypeSet.add(char.class);

        primitiveTypeSet.add(Byte.class);
        primitiveTypeSet.add(byte.class);

        primitiveTypeSet.add(Short.class);
        primitiveTypeSet.add(short.class);

        primitiveTypeSet.add(Integer.class);
        primitiveTypeSet.add(int.class);

        primitiveTypeSet.add(Long.class);
        primitiveTypeSet.add(long.class);

        primitiveTypeSet.add(Float.class);
        primitiveTypeSet.add(float.class);

        primitiveTypeSet.add(Double.class);
        primitiveTypeSet.add(double.class);

        primitiveTypeSet.add(Boolean.class);
        primitiveTypeSet.add(boolean.class);
    }

    public PrimitiveTypeConvertor(OpenApiSchemaConvertorManager manager) {
        super(manager);
    }

    @Override
    public String getConvertorName() {
        return "primitive-convertor";
    }

    @Override
    public boolean canConvert(Class clazz) {
        return primitiveTypeSet.contains(clazz);
    }

    /**
     * 基本类型转为OpenApi的基本类型
     *
     * @param simpleClass
     * @return
     */
    public Schema convert(Class simpleClass) {

        // 原生类型不需要存储组件

        // 字符串类型和字符类型都修改为字符串类型，注意这里产生了一个类型丢失
        if (simpleClass == String.class || simpleClass == Character.class || simpleClass == char.class) {
            return new Schema(DataType.String());
        } else if (simpleClass == Date.class) {
            return new Schema(DataType.Date());
        } else if (simpleClass == Byte.class || simpleClass == byte.class ||
                simpleClass == Short.class || simpleClass == short.class ||
                simpleClass == Integer.class || simpleClass == int.class) {
            // Byte、Short类型转为了int32类型，注意这里也产生了一个类型丢失
            return new Schema(DataType.Int32());
        } else if (simpleClass == Long.class || simpleClass == long.class) {
            return new Schema(DataType.Int64());
        } else if (simpleClass == Float.class || simpleClass == float.class) {
            return new Schema(DataType.Float());
        } else if (simpleClass == Double.class || simpleClass == double.class) {
            return new Schema(DataType.Double());
        } else if (simpleClass == Boolean.class || simpleClass == boolean.class) {
            return new Schema(DataType.Boolean());
        } else {
            return null;
        }
    }

}
