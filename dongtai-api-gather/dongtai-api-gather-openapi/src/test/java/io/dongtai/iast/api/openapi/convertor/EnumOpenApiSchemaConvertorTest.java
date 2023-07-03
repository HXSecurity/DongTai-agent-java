package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.Schema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EnumOpenApiSchemaConvertorTest {

    private EnumOpenApiSchemaConvertor convertor;

    @Before
    public void setUp() throws Exception {
        OpenApiSchemaConvertorManager manager = new OpenApiSchemaConvertorManager();
        convertor = manager.enumOpenApiSchemaConvertor;
    }

    @Test
    public void canConvert() {
        Assert.assertFalse(convertor.canConvert(Foo.class));
        Assert.assertFalse(convertor.canConvert(Bar.class));
        Assert.assertFalse(convertor.canConvert(R1.class));
        Assert.assertFalse(convertor.canConvert(R2.class));
        Assert.assertFalse(convertor.canConvert(Response.class));
        Assert.assertTrue(convertor.canConvert(Enumnumnum.class));
    }

    @Test
    public void convert() {
        Schema c = convertor.convert(Enumnumnum.class);
        Assert.assertNotNull(c);
        Assert.assertEquals("string", c.getType());
        Assert.assertArrayEquals(c.getEnums(), new String[]{"A", "B", "C"});
    }

}