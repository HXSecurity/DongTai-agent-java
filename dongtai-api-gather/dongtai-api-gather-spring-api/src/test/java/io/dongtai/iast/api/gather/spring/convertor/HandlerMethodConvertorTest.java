package io.dongtai.iast.api.gather.spring.convertor;

import io.dongtai.iast.api.openapi.convertor.OpenApiSchemaConvertorManager;
import io.dongtai.iast.api.openapi.domain.DataType;
import io.dongtai.iast.api.openapi.domain.Operation;
import io.dongtai.iast.api.openapi.domain.Parameter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

public class HandlerMethodConvertorTest {
    private HandlerMethodConvertor handlerMethodConvertor;
    private Operation operation;

    @Before
    public void setUp() {
        // 在每个测试方法运行之前初始化必要的对象
        OpenApiSchemaConvertorManager manager = new OpenApiSchemaConvertorManager(); // 创建实际的 manager 对象
        operation = new Operation();
        HandlerMethod handlerMethod;
        try {
            handlerMethod = new HandlerMethod(new TestController(), "testMethod",String.class,int.class); // 创建实际的 HandlerMethod 对象
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        StaticWebApplicationContext webApplicationContext = new StaticWebApplicationContext(); // 创建一个静态的 WebApplicationContext
        webApplicationContext.registerSingleton("testController", TestController.class); // 注册控制器实例
        handlerMethodConvertor = new HandlerMethodConvertor(manager, webApplicationContext, operation, handlerMethod);
    }

    @Test
    public void testParseParameters() {
        // 调用parseParameters方法
        handlerMethodConvertor.parse();

        // 获取解析后的参数列表
        List<Parameter> parameters = operation.getParameters();
        //判断是否是有两个参数
        Assert.assertEquals(2, parameters.size());
        //判断第一个参数为string是否正确
        Assert.assertEquals(DataType.String().getType(),parameters.get(0).getSchema().getType());
        //判断第二个参数是否为int32
        Assert.assertEquals(DataType.Int32().getType(),parameters.get(1).getSchema().getType());
    }

}
