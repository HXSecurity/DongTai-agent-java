package io.dongtai.iast.api.gather.dubbo.convertor;

import io.dongtai.iast.api.openapi.convertor.OpenApiSchemaConvertorManager;
import io.dongtai.iast.api.openapi.domain.DataType;
import io.dongtai.iast.api.openapi.domain.MediaType;
import io.dongtai.iast.api.openapi.domain.Operation;
import io.dongtai.iast.api.openapi.domain.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodConvertorTest {

    private OpenApiSchemaConvertorManager manager;
    private Method reflectionMethod;
    private Method reflectionReturnMethod;

    @Before
    public void setUp() throws NoSuchMethodException {
        // 创建 OpenApiSchemaConvertorManager 实例，或者使用真实的实例
        manager = new OpenApiSchemaConvertorManager();

        // 创建反射方法，或者使用真实的反射方法
        reflectionMethod = SampleService.class.getMethod("sampleMethod",String.class,String.class);
        reflectionReturnMethod = SampleService.class.getMethod("stringListSampleMethod",int.class);
    }

    @Test
    public void testConvert() {
        // 创建 MethodConvertor 实例
        MethodConvertor methodConvertor = new MethodConvertor(manager, reflectionMethod);

        // 调用 convert 方法
        Operation convertedOperation = methodConvertor.convert();

        // 进行断言，验证转换后的 Operation 对象是否符合预期
        Assert.assertNotNull(convertedOperation);
        //判断类型是否正确
        Assert.assertEquals(DataType.String().getType(),convertedOperation.getParameters().get(0).getSchema().getType());
        Assert.assertEquals(DataType.String().getType(),convertedOperation.getParameters().get(1).getSchema().getType());

        MethodConvertor methodConvertor1 = new MethodConvertor(manager, reflectionReturnMethod);
        Operation convert = methodConvertor1.convert();
        Assert.assertNotNull(convert);
        //判断有返回值的情况下是否正确
        Assert.assertEquals(DataType.Array(DataType.String())
                .getType(),convert.getResponses().get(Response.CODE_OK).getContent().get(MediaType.APPLICATION_JSON)
                .getSchema().getType());


    }

    // 示例服务类，用于测试
    static class SampleService {
        /**
         * 示例方法 无返回值
         * @param s1 字符串1
         * @param s2 字符串2
         */
        public void sampleMethod(String s1,String s2) {
            // 示例方法，用于测试
            System.out.println(s1);
            System.out.println(s1+s2);
        }

        public List<String> stringListSampleMethod(int int1){
            System.out.println(int1);
            return new ArrayList<>();
        }
    }
}
