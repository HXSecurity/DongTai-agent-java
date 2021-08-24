package com.secnium.iast.core.enhance.plugins.api;

import com.secnium.iast.core.handler.models.ApiDataModel;
import com.secnium.iast.core.handler.models.MethodEvent;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.secnium.iast.core.report.ApiReport.sendReport;

/**
 * niuerzhuang@huoxian.cn
 */
public class SpringApplicationImpl {

    public static boolean isSend;

    public static void getWebApplicationContext(MethodEvent event, AtomicInteger invokeIdSequencer) {
        ApplicationContext applicationContext = (ApplicationContext) event.returnValue;
        if(!isSend) {
        List<ApiDataModel> api = getAPI(applicationContext);
        sendReport(api);
        isSend = true;
        }
    }

    public static List<ApiDataModel> getAPI(ApplicationContext applicationContext) {
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> methodMap = mapping.getHandlerMethods();
        List<ApiDataModel> apiList = new ArrayList<>();
        for (RequestMappingInfo info : methodMap.keySet()) {
            ApiDataModel apiDataModel = new ApiDataModel();
            HandlerMethod handlerMethod = methodMap.get(info);
            String clazz = handlerMethod.getBeanType().toString().substring(6);
            apiDataModel.setClazz(clazz);
            String method = info.getMethodsCondition().toString().replace("[", "").replace("]", "");
            String[] methods;
            if ("".equals(method)) {
                methods = new String[2];
                methods[0] = "GET";
                methods[1] = "POST";
            }else {
                methods = new String[1];
                methods[0] = method;
            }
            apiDataModel.setMethod(methods);
            Method declaredMethod = null;
            try {
                HandlerMethod handlerMethodData = methodMap.get(info);
                String beanType = handlerMethodData.getBeanType().toString().substring(6);
                apiDataModel.setController(beanType);
                Method methodData = handlerMethodData.getMethod();
                String methodName = methodData.getName();
                Parameter[] parameters = methodData.getParameters();
                List<Class<?>> parameterList = new ArrayList<>();
                for (Parameter parameter : parameters
                ) {
                    parameterList.add(parameter.getType());
                }
                int parameterListSize = parameterList.size();
                Class<?>[] classes = new Class[parameterListSize];
                for (int i = 0; i < parameterListSize; i++) {
                    classes[i] = parameterList.get(i);
                }
                declaredMethod = AopUtils.getTargetClass(applicationContext.getBean(handlerMethod.getBean().toString())).getDeclaredMethod(methodName, classes);
                parameters = declaredMethod.getParameters();
                List<Map<String, String>> parameterMaps = new ArrayList<>();
                for (Parameter parameter : parameters
                ) {
                    Map<String, String> parameterMap = new HashMap<>();
                    String className = parameter.getName();
                    String classType = parameter.getType().toString();
                    if (classType.contains(" ")) {
                        classType = classType.substring(classType.indexOf(" ") + 1);
                    }
                    Annotation[] declaredAnnotations = parameter.getDeclaredAnnotations();
                    StringBuilder annos = new StringBuilder();
                    for (Annotation annotation : declaredAnnotations
                    ) {
                        String anno = annotation.annotationType().toString();
                        anno = anno.substring(anno.lastIndexOf(".")+1);
                        if ("PathVariable".equals(anno)){
                            anno = "restful访问参数";
                        }else if ("RequestHeader".equals(anno)){
                            anno = "Header参数";
                        }else if ("CookieValue".equals(anno)){
                            anno = "Cookie参数";
                        }else if ("RequestParam".equals(anno)){
                            anno = "GET请求参数";
                        }else if ("RequestBody".equals(anno)){
                            anno = "POST请求的body参数";
                        }else if ("Validated".equals(anno)){
                            anno = "GET请求参数对象";
                        }
                        annos.append(anno);
                    }
                    parameterMap.put("name", className);
                    parameterMap.put("type", classType);
                    parameterMap.put("annotation", String.valueOf(annos));
                    parameterMaps.add(parameterMap);
                }
                apiDataModel.setParameters(parameterMaps);
                String returnType = declaredMethod.getReturnType().toString();
                if (returnType.contains("class ")) {
                    returnType = declaredMethod.getReturnType().toString().substring(6);
                }
                apiDataModel.setReturnType(returnType);
            } catch (NoSuchMethodException ignore) {
            }


            PatternsRequestCondition patternsCondition = info.getPatternsCondition();
            Set<String> patterns = patternsCondition.getPatterns();
            if (patterns.size()>1){
                for (String s:patterns
                ) {
                    String uri = s.replace("[", "").replace("]", "");
                    apiDataModel.setUrl(uri);
                    apiList.add(apiDataModel);
                }
            }else {
                String uri = info.getPatternsCondition().toString().replace("[", "").replace("]", "");
                apiDataModel.setUrl(uri);
                apiList.add(apiDataModel);
            }
        }
        return apiList;
    }


}
