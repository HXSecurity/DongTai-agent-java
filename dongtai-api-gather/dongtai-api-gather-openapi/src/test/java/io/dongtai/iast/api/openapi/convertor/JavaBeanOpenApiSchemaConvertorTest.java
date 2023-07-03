package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.Schema;
import com.alibaba.fastjson2.JSON;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;

public class JavaBeanOpenApiSchemaConvertorTest {

    private OpenApiSchemaConvertorManager manager;
    private JavaBeanOpenApiSchemaConvertor convertor;

    @Before
    public void setUp() throws Exception {
        manager = new OpenApiSchemaConvertorManager();
        this.convertor = manager.javaBeanOpenApiSchemaConvertor;
    }

    @Test
    public void canConvert() {
        Assert.assertTrue(convertor.canConvert(Foo.class));
        Assert.assertTrue(convertor.canConvert(Bar.class));

        Assert.assertFalse(convertor.canConvert(ArrayList.class));
        Assert.assertFalse(convertor.canConvert(HashSet.class));
//        Assert.assertFalse(convertor.canConvert(HashMap.class));
    }

    @Test
    public void convert() {
        Schema c = convertor.convert(Foo.class);
        System.out.println(JSON.toJSONString(manager.database.toComponentSchemasMap()));
    }

    @Test
    public void testConvert() {
    }
}