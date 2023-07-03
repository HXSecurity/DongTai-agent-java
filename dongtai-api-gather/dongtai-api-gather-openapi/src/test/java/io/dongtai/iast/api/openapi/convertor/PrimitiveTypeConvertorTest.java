package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.DataType;
import io.dongtai.iast.api.openapi.domain.Schema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class PrimitiveTypeConvertorTest {

    private PrimitiveTypeConvertor convertor;

    @Before
    public void setUp() throws Exception {
        OpenApiSchemaConvertorManager manager = new OpenApiSchemaConvertorManager();
        convertor = manager.primitiveTypeConvertor;
    }

    @Test
    public void canConvert() {

        Assert.assertTrue(convertor.canConvert(boolean.class));
        Assert.assertTrue(convertor.canConvert(Boolean.class));

        Assert.assertTrue(convertor.canConvert(String.class));

        Assert.assertTrue(convertor.canConvert(Character.class));
        Assert.assertTrue(convertor.canConvert(char.class));

        Assert.assertTrue(convertor.canConvert(Float.class));
        Assert.assertTrue(convertor.canConvert(float.class));

        Assert.assertTrue(convertor.canConvert(byte.class));
        Assert.assertTrue(convertor.canConvert(Byte.class));

        Assert.assertTrue(convertor.canConvert(Short.class));
        Assert.assertTrue(convertor.canConvert(short.class));

        Assert.assertTrue(convertor.canConvert(Integer.class));
        Assert.assertTrue(convertor.canConvert(int.class));

        Assert.assertTrue(convertor.canConvert(Long.class));
        Assert.assertTrue(convertor.canConvert(long.class));

        Assert.assertFalse(convertor.canConvert(List.class));
        Assert.assertFalse(convertor.canConvert(Foo.class));
        Assert.assertFalse(convertor.canConvert(Bar.class));
    }

    @Test
    public void convert() {

        Assert.assertEquals(new Schema(DataType.Boolean()), convertor.convert(boolean.class));
        Assert.assertEquals(new Schema(DataType.Boolean()), convertor.convert(Boolean.class));

        Assert.assertEquals(new Schema(DataType.String()), convertor.convert(String.class));

        Assert.assertEquals(new Schema(DataType.String()), convertor.convert(Character.class));
        Assert.assertEquals(new Schema(DataType.String()), convertor.convert(char.class));

        Assert.assertEquals(new Schema(DataType.Float()), convertor.convert(Float.class));
        Assert.assertEquals(new Schema(DataType.Float()), convertor.convert(float.class));

        Assert.assertEquals(new Schema(DataType.Int32()), convertor.convert(byte.class));
        Assert.assertEquals(new Schema(DataType.Int32()), convertor.convert(Byte.class));

        Assert.assertEquals(new Schema(DataType.Int32()), convertor.convert(Short.class));
        Assert.assertEquals(new Schema(DataType.Int32()), convertor.convert(short.class));

        Assert.assertEquals(new Schema(DataType.Int32()), convertor.convert(Integer.class));
        Assert.assertEquals(new Schema(DataType.Int32()), convertor.convert(int.class));

        Assert.assertEquals(new Schema(DataType.Int64()), convertor.convert(Long.class));
        Assert.assertEquals(new Schema(DataType.Int64()), convertor.convert(long.class));

    }
}