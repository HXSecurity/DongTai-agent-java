package io.dongtai.iast.openapi.convertor;

import io.dongtai.iast.openapi.domain.Schema;
import io.dongtai.log.DongTaiLog;

/**
 * 转换器的入口类
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class OpenApiSchemaConvertorManager {

    // 内部用来存储组件的数据
    ComponentDatabase database;

    // 一堆类型转换器
    PrimitiveTypeConvertor primitiveTypeConvertor;
    EnumOpenApiSchemaConvertor enumOpenApiSchemaConvertor;
    ArrayOpenApiSchemaConvertor arrayOpenApiSchemaConvertor;
    JavaBeanOpenApiSchemaConvertor javaBeanOpenApiSchemaConvertor;
    CollectionOpenApiSchemaConvertor collectionOpenApiSchemaConvertor;

    // 转换器使用的顺序
    private ClassOpenApiSchemaConvertor[] convertors;

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
     * 为给定的类型生成类型
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
            if (convertor.canConvert(clazz)) {
                try {
                    Schema c = convertor.convert(clazz);
                    if (c != null) {
                        return c.direct();
                    }
                } catch (Throwable e) {
                    DongTaiLog.error("OpenApiSchemaConvertorManager.convertClass error, convert {}, class {}", convertor.getConvertorName(), clazz.getName(), e);
                }
            }
        }

        // 转换不了就算球
        return null;
//
//        // 数组
//        if (clazz.isArray()) {
//            return arrayOpenApiSchemaConvertor.convert(clazz);
//        }
//
//        // 基本类型直接转换
//        if (manager.primitiveTypeConvertor.canConvert(componentType)) {
//            Component items = manager.primitiveTypeConvertor.convert(componentType);
//            return new Component(DataType.Array(items));
//        }
//
//        // 集合类型调用其处理
//        if (manager.collectionOpenApiSchemaConvertor.canConvert(componentType)) {
//            Component items = manager.collectionOpenApiSchemaConvertor.convert(componentType);
//            return new Component(DataType.Array(items));
//        }
//
//        // bean类型处理
//        if (manager.javaBeanOpenApiSchemaConvertor.canConvert(clazz)) {
//            Component items = manager.javaBeanOpenApiSchemaConvertor.convert(componentType);
//            return new Component(DataType.Array(items));
//        }

//        // 尝试进行基本类型的转换，如果能够转换成功的话说明是基本类型，则直接返回即可
//        Component c = convertPrimitiveType(clazz);
//        if (c != null) {
//            return c;
//        }
//
//        // 查询是否已经处理过，如果已经处理过的话则直接返回之前的结果
//        if (classToComponentMap.containsKey(clazz)) {
//            return classToComponentMap.get(clazz);
//        }
//
//        // 数组的处理
//        if (clazz.isArray()) {
//            c = convertArray(clazz);
//            cache(clazz, c);
//            return c;
//        }
//
//        // 尝试进行复杂类型转换
//        c = convertBean(clazz);
//        // 放入到缓存中
//        cache(clazz, c);
//
//        return c;
    }

    public ComponentDatabase getDatabase() {
        return database;
    }
}
