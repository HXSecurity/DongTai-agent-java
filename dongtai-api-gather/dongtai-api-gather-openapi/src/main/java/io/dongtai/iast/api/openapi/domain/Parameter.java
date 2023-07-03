package io.dongtai.iast.api.openapi.domain;

/**
 * 用于表示接口的一个参数
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class Parameter {

    // 参数的名称
    private String name;

    // 参数出现的位置
    private ParameterIn in;

    // 参数是否强制
    private boolean required;

    // 参数的描述信息
    private String description;

    // 参数的类型
    private Schema schema;

    // 参数是否被标记为不推荐
    private Boolean deprecated;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public ParameterIn getIn() {
        return in;
    }

    public void setIn(ParameterIn in) {
        this.in = in;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

}
