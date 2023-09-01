package io.dongtai.iast.api.gather.dubbo.convertor;

import io.dongtai.iast.api.openapi.convertor.OpenApiSchemaConvertorManager;
import io.dongtai.iast.api.openapi.domain.Operation;
import io.dongtai.iast.api.openapi.domain.Path;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 类级别的转换，将dubbo的Service接口转换为open api的格式
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class ServiceConvertor {

    private final OpenApiSchemaConvertorManager manager;
    private final Class interfaceClass;

    public ServiceConvertor(OpenApiSchemaConvertorManager manager, Class interfaceClass) {
        this.manager = manager;
        this.interfaceClass = interfaceClass;
    }

    public Map<String, Path> convert() {
        Map<String, Path> pathMap = new HashMap<>();
        for (Method parseServiceMethod : this.parseServiceMethods()) {
            try {
                Operation convert = new MethodConvertor(this.manager, parseServiceMethod).convert();
                Path path = new Path();
                path.setDubbo(convert);
                pathMap.put(this.buildSign(parseServiceMethod), path);
            } catch (Throwable e) {
                DongTaiLog.debug("ServiceConvertor.convert exception", e);
            }
        }
        return pathMap;
    }

    /**
     * 解析Service上提供的接口
     *
     * @return
     */
    private List<Method> parseServiceMethods() {
        List<Method> methodList = new ArrayList<>();
        Set<String> distinctSet = new HashSet<>();
        Queue<Class> needProcessClassQueue = new LinkedList<>();
        needProcessClassQueue.add(this.interfaceClass);
        while (!needProcessClassQueue.isEmpty()) {
            Class poll = needProcessClassQueue.poll();

            // 收集当前类上的方法
            Method[] declaredMethods = poll.getDeclaredMethods();
            for (Method declaredMethod : declaredMethods) {
                String s = this.buildSign(declaredMethod);
                if (distinctSet.contains(s)) {
                    continue;
                }
                distinctSet.add(s);
                methodList.add(declaredMethod);
            }

            // 收集父接口，以便等下处理父接口上的方法
            needProcessClassQueue.addAll(Arrays.asList(poll.getInterfaces()));
        }

        return methodList;
    }

    /**
     * 方法的签名需要统一，签名的格式与dubbo流量采集那里保持一致，在server端要靠这个作为path把它们关联到一起
     *
     * @param method
     * @return Example: /app.iast.common.dubbo.vul.VulService/runtimeExec(java.lang.String,java.lang.StringBuilder,byte[])
     */
    private String buildSign(Method method) {
        StringBuilder sign = new StringBuilder();
        sign.append("/").append(method.getDeclaringClass().getName()).append("/").append(method.getName()).append("(");
        Parameter[] parameters = method.getParameters();
        if (parameters != null && parameters.length != 0) {
            for (int i = 0; i < parameters.length; i++) {
                sign.append(parameters[i].getType().getCanonicalName());
                if (i + 1 < parameters.length) {
                    sign.append(",");
                }
            }
        }
        return sign.append(")").toString();
    }

}
