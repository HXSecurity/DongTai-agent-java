package io.dongtai.iast.api.openapi.domain;

/**
 * @author CC11001100
 * @since v1.12.0
 */
public class MediaType {

    public static final String ALL = "*/*";
    public static final String APPLICATION_JSON = "application/json";

    // 只要一个类型，其他字段暂时不设置
    private Schema schema;

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

}
