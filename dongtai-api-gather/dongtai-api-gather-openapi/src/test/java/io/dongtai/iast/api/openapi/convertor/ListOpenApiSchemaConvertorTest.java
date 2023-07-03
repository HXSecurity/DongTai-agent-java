package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.Schema;
import org.junit.Before;
import org.junit.Test;

public class ListOpenApiSchemaConvertorTest {

    private ListOpenApiSchemaConvertor convertor;

    @Before
    public void setUp() throws Exception {
        OpenApiSchemaConvertorManager manager = new OpenApiSchemaConvertorManager();
        convertor = manager.collectionOpenApiSchemaConvertor.getListOpenApiSchemaConvertor();
    }

    @Test
    public void canConvert() {
    }

    @Test
    public void convert() throws NoSuchFieldException {

//        List<String> l = new ArrayList<>();
//        Schema c = convertor.convert(l.getClass());
//        System.out.println(c);

        Foo foo = new Foo();
        Schema c = convertor.convert(foo.getClass(), foo.getClass().getDeclaredField("list4"));
        System.out.println(c);

    }

    @Test
    public void testConvert() {
    }
}