package io.dongtai.iast.api.gather.spring.convertor;

import io.dongtai.iast.api.openapi.convertor.OpenApiSchemaConvertorManager;
import io.dongtai.iast.api.openapi.domain.*;
import io.dongtai.log.DongTaiLog;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.WebApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * 解析Handler方法的上的形参
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class MethodParameterConvertor {

    private final OpenApiSchemaConvertorManager manager;
    private final WebApplicationContext webApplicationContext;
    private final Operation operation;
    private final MethodParameter methodParameter;

    public MethodParameterConvertor(OpenApiSchemaConvertorManager manager, WebApplicationContext webApplicationContext, Operation operation, MethodParameter methodParameter) {
        this.manager = manager;
        this.webApplicationContext = webApplicationContext;
        this.operation = operation;
        this.methodParameter = methodParameter;
    }

    /**
     * 解析单个的形参，将其解析为一个参数
     *
     * @return
     */
    public Parameter parse() {

        // Spring的内部类不做转换，只是简单的传递一下名字
        if (isWebFrameworkClass()) {
//            Component c = this.manager.getDatabase().register(this.methodParameter.getParameterType());
//            Parameter p = new Parameter();
//            p.setName(this.methodParameter.getParameterType().getSimpleName());
//            p.setSchema(c);
//            return p;
            // 2023-6-20 18:34:20 如果是spring内部类的话，则直接忽略不再上报
            return null;
        }

        // 先按照形参来解析
        Parameter parameter = parseMethodParameter();
        // 只按照形参解析得到的值不一定准确，接下来使用注解来修正参数
        return overrideParameterByAnnotation(parameter);
    }

    /**
     * 解析方法的形参，从形参中得到HTTP参数
     *
     * @return
     */
    private Parameter parseMethodParameter() {
        Parameter p = new Parameter();

        // 默认是在 query 中
        p.setIn(ParameterIn.Query);

        // 并且是必传的
        p.setRequired(true);

        // http参数的名称和形参的名称保持一致，如果有在注解中指定的话后面会去修正，这里只需要设置与形参名称一致即可
        try {
            LocalVariableTableParameterNameDiscoverer localVariableTableParameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
            String[] parameterNames = localVariableTableParameterNameDiscoverer.getParameterNames(this.methodParameter.getMethod());
            String parameterName = parameterNames[this.methodParameter.getParameterIndex()];
            p.setName(parameterName);
        } catch (Throwable e) {
            DongTaiLog.debug("parse method parameter exception: {}", e.getMessage());
        }

        // 参数的类型需要存储一下
        Schema c = this.manager.convertClass(this.methodParameter.getParameterType());
        p.setSchema(c);

        return p;
    }

    /**
     * 判断参数值的类型是否是Spring框架的类，如果是的话则不能当做组件来处理，这里主要是为了忽略ModelAndView、HttpServletRequest这些类
     *
     * @return
     */
    private boolean isWebFrameworkClass() {
        String parameterClassName = this.methodParameter.getParameterType().getName();
        return parameterClassName.startsWith(" org.springframework.".substring(1)) ||
                parameterClassName.startsWith(" javax.servlet.".substring(1)) ||
                parameterClassName.startsWith(" jakarta.servlet.".substring(1));
    }

    /**
     * 根据形参上的注解重写参数
     *
     * @param p
     */
    private Parameter overrideParameterByAnnotation(Parameter p) {
        for (Annotation parameterAnnotation : this.methodParameter.getParameterAnnotations()) {
            Class<? extends Annotation> aClass = parameterAnnotation.annotationType();
            switch (aClass.getName()) {
                case "org.springframework.web.bind.annotation.PathVariable":
                    p.setIn(ParameterIn.Path);
                    break;
                case "org.springframework.web.bind.annotation.RequestBody":
                    // 这个参数是放在请求体部分的，不是参数部分，所以取消参数，并创建请求体
                    this.operation.setRequestBody(this.parseRequestBody());
                    return null;
                case "org.springframework.web.bind.annotation.RequestHeader":
                    p.setIn(ParameterIn.Header);
                    break;
                case "org.springframework.web.bind.annotation.CookieValue":
                    p.setIn(ParameterIn.Cookie);
                    break;
                case "org.springframework.web.bind.annotation.RequestParam":
                    p.setIn(ParameterIn.Query);
                    break;
                case "org.springframework.validation.annotation.Validated":
                    p.setIn(ParameterIn.Query);
                    break;
            }

            // 解析参数名称
            try {
                String name = (String) aClass.getMethod("name").invoke(parameterAnnotation);
                if (!"".equals(name)) {
                    p.setName(name);
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                // ignored
            }

            // 参数是否是必传的
            try {
                Boolean required = (Boolean) aClass.getMethod("required").invoke(parameterAnnotation);
                if (required != null) {
                    p.setRequired(required);
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                // ignored
            }

            // 2023-6-14 17:53:05 暂时不处理默认值
//                // 获取默认值
//                try {
//                    Boolean defaultValue = (Boolean) aClass.getMethod("defaultValue").invoke(a);
//                    p.defa(defaultValue);
//                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//                    throw new RuntimeException(e);
//                }

        }

        return p;
    }

    /**
     * 当前参数是@RequestBody参数注解的，说明它是一个请求的请求体参数
     *
     * @return
     */
    private RequestBody parseRequestBody() {
        RequestBody r = new RequestBody();
        r.setRequired(true);

        MediaType mediaType = new MediaType();
        mediaType.setSchema(this.manager.convertClass(this.methodParameter.getParameterType()));

        Map<String, MediaType> contentMap = new HashMap<>();
        contentMap.put(MediaType.APPLICATION_JSON, mediaType);

        r.setContent(contentMap);

        return r;
    }

}
