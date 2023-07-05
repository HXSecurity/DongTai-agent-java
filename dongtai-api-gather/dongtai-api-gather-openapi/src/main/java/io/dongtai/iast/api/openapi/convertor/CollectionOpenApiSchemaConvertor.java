package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.Schema;

import java.util.Collection;

/**
 * 用于Java内置的集合类型的转换，比如List、Set、Map
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class CollectionOpenApiSchemaConvertor extends BaseOpenApiSchemaConvertor {

    final ListOpenApiSchemaConvertor listOpenApiSchemaConvertor;
    final SetOpenApiSchemaConvertor setOpenApiSchemaConvertor;
    final MapOpenApiSchemaConvertor mapOpenApiSchemaConvertor;

    public CollectionOpenApiSchemaConvertor(OpenApiSchemaConvertorManager manager) {
        super(manager);
        this.listOpenApiSchemaConvertor = new ListOpenApiSchemaConvertor(manager);
        this.setOpenApiSchemaConvertor = new SetOpenApiSchemaConvertor(manager);
        this.mapOpenApiSchemaConvertor = new MapOpenApiSchemaConvertor(manager);
    }

    @Override
    public String getConvertorName() {
        return "collection-convertor";
    }

    @Override
    public boolean canConvert(Class clazz) {
        return clazz != null && (
                listOpenApiSchemaConvertor.canConvert(clazz) ||
                        setOpenApiSchemaConvertor.canConvert(clazz) ||
                        mapOpenApiSchemaConvertor.canConvert(clazz) ||
                        isCollectionClass(clazz)
        );
    }

    private boolean isCollectionClass(Class clazz) {
        return clazz != null && Collection.class.isAssignableFrom(clazz);
    }

    @Override
    public Schema convert(Class clazz) {
        if (listOpenApiSchemaConvertor.canConvert(clazz)) {
            return listOpenApiSchemaConvertor.convert(clazz);
        } else if (setOpenApiSchemaConvertor.canConvert(clazz)) {
            return setOpenApiSchemaConvertor.convert(clazz);
        } else if (mapOpenApiSchemaConvertor.canConvert(clazz)) {
            return mapOpenApiSchemaConvertor.convert(clazz);
        } else if (isCollectionClass(clazz)) {
            return listOpenApiSchemaConvertor.convert(clazz);
        } else {
            return null;
        }
    }

    public ListOpenApiSchemaConvertor getListOpenApiSchemaConvertor() {
        return listOpenApiSchemaConvertor;
    }

    public SetOpenApiSchemaConvertor getSetOpenApiSchemaConvertor() {
        return setOpenApiSchemaConvertor;
    }

    public MapOpenApiSchemaConvertor getMapOpenApiSchemaConvertor() {
        return mapOpenApiSchemaConvertor;
    }

}
