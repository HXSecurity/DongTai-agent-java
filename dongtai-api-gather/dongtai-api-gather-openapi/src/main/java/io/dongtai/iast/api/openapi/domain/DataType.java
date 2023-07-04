package io.dongtai.iast.api.openapi.domain;

import java.util.Objects;

/**
 * 用于表示Open API中的数据类型
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class DataType {

    // 类型的名称，比如integer
    private String type;

    // 类型的格式，比如int32
    private String format;

    // 如果是数组类型的话，则数组中的元素都是啥类型的，数组中的元素可以是复合类型（引用）也可以是基本类型
    private DataType items;

    // 如果是数组的话，是否是唯一的，其实就是用来标识是不是set类型的，因为是很多语言中都是有set类型的
    private Boolean uniqueItems;

    public DataType() {
    }

    private DataType(String type, String format, DataType items) {
        this.type = type;
        this.format = format;
        this.items = items;
    }

    public static DataType Null() {
        return new DataType("null", null, null);
    }

    public static DataType Int32() {
        return new DataType("integer", "int32", null);
    }

    public static DataType Int32Array() {
        return Array(Int32());
    }

    public static DataType Int64() {
        return new DataType("integer", "int64", null);
    }

    public static DataType Int64Array() {
        return Array(Int64());
    }

    public static DataType Float() {
        return new DataType("number", "float", null);
    }

    public static DataType FloatArray() {
        return Array(Float());
    }

    public static DataType Double() {
        return new DataType("number", "double", null);
    }

    public static DataType DoubleArray() {
        return Array(Double());
    }

    public static DataType String() {
        return new DataType("string", null, null);
    }

    public static DataType Date() {
        return new DataType("string", "date-time", null);
    }

    public static DataType StringArray() {
        return Array(String());
    }

    public static DataType Password() {
        return new DataType("string", "password", null);
    }

    public static DataType Boolean() {
        return new DataType("boolean", null, null);
    }

    public static DataType BooleanArray() {
        return Array(Boolean());
    }

    public static DataType Array(DataType itemType) {
        return new DataType("array", null, itemType);
    }

    public static DataType Object() {
        return new DataType("object", null, null);
    }

    public static DataType ObjectArray() {
        return new DataType("array", null, Object());
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public DataType getItems() {
        return items;
    }

    public void setItems(DataType items) {
        this.items = items;
    }

    public Boolean getUniqueItems() {
        return uniqueItems;
    }

    public void setUniqueItems(Boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataType dataType = (DataType) o;
        return Objects.equals(type, dataType.type) && Objects.equals(format, dataType.format) && Objects.equals(items, dataType.items) && Objects.equals(uniqueItems, dataType.uniqueItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, format, items, uniqueItems);
    }

}
