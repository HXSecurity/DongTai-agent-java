package io.dongtai.iast.api.openapi.domain;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Open API中定义的一个Schema，Schema可能是一个复合类型，也可能是一个简单类型
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class Schema extends DataType {

    // 类型的名称，反序列化的时候忽略此字段
    @JSONField(serialize = false)
    private String name;

    // 如果是引用了Schema的话则是个uri
    private String $ref;

    @JSONField(name = "default")
    private String defaultValue;

    @JSONField(name = "enum")
    private String[] enums;

    // Schema的字段，如果是复合类型的话，复合类型的属性既可以是简单类型，也可以是复合类型，所以使用Schema来引用
    private Map<String, Schema> properties;

    public Schema() {
    }

    /**
     * 从类型创建schema，一般用来包装基本类型和基本类型数组
     *
     * @param dataType
     */
    public Schema(DataType dataType) {
        this.setType(dataType.getType());
        this.setFormat(dataType.getFormat());
        this.setItems(dataType.getItems());
    }

    /**
     * 此Schema只是用来引用其它组件的，只用来表示一个指针
     *
     * @param $ref
     */
    public Schema(String $ref) {
        this.set$ref($ref);
    }

    /**
     * 此组件是否可以被其他组件引用，只有有名字的组件才可以被引用
     *
     * @return
     */
    public boolean canRef() {
        return this.name != null && !"".equals(this.name);
    }

    /**
     * 生成给$ref用的引用路径，这样其它组件就可以用用自己
     *
     * @return
     */
    public String generateRef() {
        return "#/components/schemas/" + name;
    }

    /**
     * 如果是可以引用的类型，则返回一个引用，否则返回本身
     *
     * @return
     */
    public Schema direct() {
        if (this.canRef()) {
            // 返回引用指针，而不是直接返回它自身
            return new Schema(this.generateRef());
        } else {
            // 非引用类型直接返回自身
            return this;
        }
    }

    /**
     * 为组件增加一个属性
     *
     * @param key
     * @param value
     */
    public void addProperty(String key, Schema value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
    }

    public String get$ref() {
        return $ref;
    }

    public void set$ref(String $ref) {
        this.$ref = $ref;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String[] getEnums() {
        return enums;
    }

    public void setEnums(String[] enums) {
        this.enums = enums;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Schema> getProperties() {
        return properties;
    }

    // 不给直接访问，通过merge
//    public void setProperties(Map<String, Schema> properties) {
//        this.properties = properties;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Schema)) return false;
        if (!super.equals(o)) return false;
        Schema schema = (Schema) o;
        return Objects.equals(name, schema.name) && Objects.equals($ref, schema.$ref) && Objects.equals(defaultValue, schema.defaultValue) && Arrays.equals(enums, schema.enums) && Objects.equals(properties, schema.properties);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), name, $ref, defaultValue, properties);
        result = 31 * result + Arrays.hashCode(enums);
        return result;
    }

    /**
     * 比较JSON是否相等
     *
     * @param c
     * @return
     */
    public boolean jsonEquals(Schema c) {
        return JSON.toJSONString(this).equals(JSON.toJSONString(c));
    }

    public String toJson() {
        return JSON.toJSONString(this);
    }

}
