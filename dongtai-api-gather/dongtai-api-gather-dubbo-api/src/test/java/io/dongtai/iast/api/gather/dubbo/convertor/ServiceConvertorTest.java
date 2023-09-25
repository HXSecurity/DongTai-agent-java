package io.dongtai.iast.api.gather.dubbo.convertor;

import io.dongtai.iast.api.openapi.convertor.OpenApiSchemaConvertorManager;
import io.dongtai.iast.api.openapi.domain.Parameter;
import io.dongtai.iast.api.openapi.domain.Path;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class ServiceConvertorTest {
    @Test
    public void testConvert() {
        // 创建一个 OpenApiSchemaConvertorManager，这里可以根据需要进行配置
        OpenApiSchemaConvertorManager manager = new OpenApiSchemaConvertorManager();

        // 创建一个 ServiceConvertor 实例，传入需要转换的接口类
        Class<?> interfaceClass = TestDubboServiceInterface.class; // 替换成实际的接口类
        ServiceConvertor serviceConvertor = new ServiceConvertor(manager, interfaceClass);

        // 调用 convert 方法进行转换
        Map<String, Path> pathMap = serviceConvertor.convert();

        // 打印转换结果或进行其他断言
        for (Map.Entry<String, Path> entry : pathMap.entrySet()) {
            String path = entry.getKey();
            Path pathData = entry.getValue();
            // 进行其他断言或操作
            Method method = AnalysisMethod(path);
            List<Parameter> parameters = pathData.getDubbo().getParameters();
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0, parametersSize = parameters.size(); i < parametersSize; i++) {
                Parameter parameter = parameters.get(i);
                String type = parameter.getSchema().getType();
                Class<?> parameterType = parameterTypes[i];
                String s = interceptType(parameterType);
                Assert.assertTrue(s.equalsIgnoreCase(type));
            }

        }
    }

    /**
     * 将JAVA类型转换为Openapi类型
     * @param parameterType java类型
     * @return Openapi类型
     */
    private static String interceptType(Class<?> parameterType) {
        String[] split = parameterType.getName().split("\\.");
        return split[split.length - 1];
    }

    /**
     * 通过PATH 查找方法
     * @param path 方法路径
     * @return 方法
     */
    private Method AnalysisMethod(String path) {
        String[] split = path.split("/");
        Assert.assertEquals(3, split.length);
        String methodNameWithParams = split[2];

        String[] methodParts = methodNameWithParams.split("\\(");
        String methodName = methodParts[0];
        String paramsStr = methodParts[1].replace(")", "");
        String[] paramTypeNames = paramsStr.split(",");
        Class<?>[] paramTypes = new Class[paramTypeNames.length];
        for (int i = 0; i < paramTypeNames.length; i++) {
            try {
                paramTypes[i] = Class.forName(paramTypeNames[i]);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        Method method;
        try {
            method = TestDubboServiceInterface.class.getMethod(methodName, paramTypes);

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return method;

    }

    // Dubbo 服务接口类
    private interface TestDubboServiceInterface {
        // 定义需要测试的 Dubbo 服务方法
        String sayHi(String name);
    }
}
