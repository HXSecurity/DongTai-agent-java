package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.Schema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class SetOpenApiSchemaConvertorTest {

    private SetOpenApiSchemaConvertor convertor;

    @Before
    public void setUp() throws Exception {
        OpenApiSchemaConvertorManager manager = new OpenApiSchemaConvertorManager();
        this.convertor = manager.collectionOpenApiSchemaConvertor.getSetOpenApiSchemaConvertor();
    }

    @Test
    public void canConvert() {
        Assert.assertTrue(this.convertor.canConvert(HashSet.class));
        Assert.assertTrue(this.convertor.canConvert(LinkedHashSet.class));

        Assert.assertFalse(this.convertor.canConvert(ArrayList.class));
    }

    @Test
    public void convert() {

        String setArray = "{\"items\":{\"type\":\"object\"},\"type\":\"array\",\"uniqueItems\":true}";

        Schema c = this.convertor.convert(HashSet.class);
        Assert.assertEquals(setArray, c.toJson());

        c = this.convertor.convert(LinkedHashSet.class);
        Assert.assertEquals(setArray, c.toJson());

    }
    
}