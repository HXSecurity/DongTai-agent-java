package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.Schema;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

/**
 * 转换器的入口类
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class OpenApiSchemaConvertorManager {

    // 内部用来存储组件的数据
    final ComponentDatabase database;

    // 一堆类型转换器
    final PrimitiveTypeConvertor primitiveTypeConvertor;
    final EnumOpenApiSchemaConvertor enumOpenApiSchemaConvertor;
    final ArrayOpenApiSchemaConvertor arrayOpenApiSchemaConvertor;
    final JavaBeanOpenApiSchemaConvertor javaBeanOpenApiSchemaConvertor;
    final CollectionOpenApiSchemaConvertor collectionOpenApiSchemaConvertor;

    // 转换器使用的顺序
    final ClassOpenApiSchemaConvertor[] convertors;

    public OpenApiSchemaConvertorManager() {

        this.database = new ComponentDatabase(this);

        primitiveTypeConvertor = new PrimitiveTypeConvertor(this);
        enumOpenApiSchemaConvertor = new EnumOpenApiSchemaConvertor(this);
        arrayOpenApiSchemaConvertor = new ArrayOpenApiSchemaConvertor(this);
        javaBeanOpenApiSchemaConvertor = new JavaBeanOpenApiSchemaConvertor(this);
        collectionOpenApiSchemaConvertor = new CollectionOpenApiSchemaConvertor(this);

        convertors = new ClassOpenApiSchemaConvertor[]{
                primitiveTypeConvertor,
                javaBeanOpenApiSchemaConvertor,
                arrayOpenApiSchemaConvertor,
                collectionOpenApiSchemaConvertor,
                enumOpenApiSchemaConvertor
        };
    }


    /**
     * 将给定的类型转换为Open API的类型
     *
     * @param clazz
     * @return
     */
    public Schema convertClass(Class clazz) {

        if (clazz == null) {
            return null;
        }

        // 依此使用转换器尝试转换
        for (ClassOpenApiSchemaConvertor convertor : convertors) {
            try {
                if (convertor.canConvert(clazz)) {
                    Schema c = convertor.convert(clazz);
                    if (c != null) {
                        return c.direct();
                    }
                }
            } catch (Throwable e) {
                DongTaiLog.error(ErrorCode.API_GATHER_OPENAPI_CONVERT_ERROR, convertor.getConvertorName(), clazz.getName(), e);
            }
        }

        // 转换不了就算球
        return null;
    }

    public ComponentDatabase getDatabase() {
        return database;
    }

}
