package io.dongtai.iast.openapi.convertor;

import io.dongtai.iast.openapi.domain.Schema;

/**
 * 用于Java内置的集合类型的转换，比如List、Set、Map
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class CollectionOpenApiSchemaConvertor extends BaseOpenApiSchemaConvertor {

    private final ListOpenApiSchemaConvertor listOpenApiSchemaConvertor;
    private final SetOpenApiSchemaConvertor setOpenApiSchemaConvertor;
    private final MapOpenApiSchemaConvertor mapOpenApiSchemaConvertor;

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
                        mapOpenApiSchemaConvertor.canConvert(clazz)
        );
    }

    @Override
    public Schema convert(Class clazz) {
        if (listOpenApiSchemaConvertor.canConvert(clazz)) {
            return listOpenApiSchemaConvertor.convert(clazz);
        } else if (setOpenApiSchemaConvertor.canConvert(clazz)) {
            return setOpenApiSchemaConvertor.convert(clazz);
        } else if (mapOpenApiSchemaConvertor.canConvert(clazz)) {
            return mapOpenApiSchemaConvertor.convert(clazz);
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
