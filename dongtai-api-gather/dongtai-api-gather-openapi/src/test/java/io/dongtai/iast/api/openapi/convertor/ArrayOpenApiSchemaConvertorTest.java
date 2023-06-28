package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.DataType;
import io.dongtai.iast.api.openapi.domain.Schema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ArrayOpenApiSchemaConvertorTest {

    private ArrayOpenApiSchemaConvertor convertor;

    @Before
    public void setUp() throws Exception {
        OpenApiSchemaConvertorManager manager = new OpenApiSchemaConvertorManager();
        convertor = manager.arrayOpenApiSchemaConvertor;
    }

    @Test
    public void canConvert() {
        Assert.assertTrue(convertor.canConvert(boolean[].class));
        Assert.assertTrue(convertor.canConvert(Boolean[].class));

        Assert.assertTrue(convertor.canConvert(String[].class));

        Assert.assertTrue(convertor.canConvert(Character[].class));
        Assert.assertTrue(convertor.canConvert(char[].class));

        Assert.assertTrue(convertor.canConvert(Float[].class));
        Assert.assertTrue(convertor.canConvert(float[].class));

        Assert.assertTrue(convertor.canConvert(byte[].class));
        Assert.assertTrue(convertor.canConvert(Byte[].class));

        Assert.assertTrue(convertor.canConvert(Short[].class));
        Assert.assertTrue(convertor.canConvert(short[].class));

        Assert.assertTrue(convertor.canConvert(Integer[].class));
        Assert.assertTrue(convertor.canConvert(int[].class));

        Assert.assertTrue(convertor.canConvert(Long[].class));
        Assert.assertTrue(convertor.canConvert(long[].class));

        Assert.assertTrue(convertor.canConvert(List[].class));
        Assert.assertTrue(convertor.canConvert(Foo[].class));
        Assert.assertTrue(convertor.canConvert(Bar[].class));
    }

    @Test
    public void convert() {

        // 基本类型数组
        Assert.assertTrue(new Schema(DataType.BooleanArray()).jsonEquals(convertor.convert(boolean[].class)));
        Assert.assertTrue(new Schema(DataType.BooleanArray()).jsonEquals(convertor.convert(Boolean[].class)));

        Assert.assertTrue(new Schema(DataType.StringArray()).jsonEquals(convertor.convert(String[].class)));

        Assert.assertTrue(new Schema(DataType.StringArray()).jsonEquals(convertor.convert(Character[].class)));
        Assert.assertTrue(new Schema(DataType.StringArray()).jsonEquals(convertor.convert(char[].class)));

        Assert.assertTrue(new Schema(DataType.FloatArray()).jsonEquals(convertor.convert(Float[].class)));
        Assert.assertTrue(new Schema(DataType.FloatArray()).jsonEquals(convertor.convert(float[].class)));

        Assert.assertTrue(new Schema(DataType.Int32Array()).jsonEquals(convertor.convert(byte[].class)));
        Assert.assertTrue(new Schema(DataType.Int32Array()).jsonEquals(convertor.convert(Byte[].class)));

        Assert.assertTrue(new Schema(DataType.Int32Array()).jsonEquals(convertor.convert(Short[].class)));
        Assert.assertTrue(new Schema(DataType.Int32Array()).jsonEquals(convertor.convert(short[].class)));

        Assert.assertTrue(new Schema(DataType.Int32Array()).jsonEquals(convertor.convert(Integer[].class)));
        Assert.assertTrue(new Schema(DataType.Int32Array()).jsonEquals(convertor.convert(int[].class)));

        Assert.assertTrue(new Schema(DataType.Int64Array()).jsonEquals(convertor.convert(Long[].class)));
        Assert.assertTrue(new Schema(DataType.Int64Array()).jsonEquals(convertor.convert(long[].class)));

        // 枚举类型数组
        Assert.assertEquals("{\"items\":{\"enum\":[\"A\",\"B\",\"C\"],\"type\":\"string\"},\"type\":\"array\"}", convertor.convert(Enumnumnum[].class).toJson());

        // 多维数组
        Assert.assertEquals("{\"items\":{\"type\":\"object\"},\"type\":\"array\"}", convertor.convert(String[][].class).toJson());

        // 集合类型数组
        Assert.assertEquals("{\"items\":{\"items\":{\"type\":\"object\"},\"type\":\"array\"},\"type\":\"array\"}", convertor.convert(ArrayList[].class).toJson());

        // TODO Bean类型数组

    }

}