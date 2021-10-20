package cn.huoxian.iast.servlet.api;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class SpringApplicationContext {

    public static Map<String, Object> getAPI(Object applicationContext) {
        return createReport(getAPIList((WebApplicationContext) applicationContext));
    }

    public static List<ApiDataModel> getAPIList(WebApplicationContext applicationContext) {
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
            } else {
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
                        anno = anno.substring(anno.lastIndexOf(".") + 1);
                        if ("PathVariable".equals(anno)) {
                            anno = "restful访问参数";
                        } else if ("RequestHeader".equals(anno)) {
                            anno = "Header参数";
                        } else if ("CookieValue".equals(anno)) {
                            anno = "Cookie参数";
                        } else if ("RequestParam".equals(anno)) {
                            anno = "GET请求参数";
                        } else if ("RequestBody".equals(anno)) {
                            anno = "POST请求的body参数";
                        } else if ("Validated".equals(anno)) {
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
            if (patterns.size() > 1) {
                for (String s : patterns
                ) {
                    String uri =applicationContext.getApplicationName() + s.replace("[", "").replace("]", "");
                    apiDataModel.setUrl(uri);
                    apiList.add(apiDataModel);
                }
            } else {
                String uri = applicationContext.getApplicationName() + info.getPatternsCondition().toString().replace("[", "").replace("]", "");
                apiDataModel.setUrl(uri);
                apiList.add(apiDataModel);
            }
        }
        return apiList;
    }

    private static Map<String, Object> createReport(List<ApiDataModel> apiList) {
        Map<String, Object> apiDataReport = new HashMap<>();
        List<Object> apiData = new ArrayList<>();
        for (ApiDataModel apiDataModel:apiList
        ) {
            Map<String, Object> api = new HashMap<>();
            apiData.add(api);
            api.put("uri",apiDataModel.getUrl());
            String[] methods = apiDataModel.getMethod();
            List<Object> methodsjson = new ArrayList<>(Arrays.asList(methods));
            api.put("method",methodsjson);
            api.put("class",apiDataModel.getClazz());
            List<Map<String, String>> parameters = apiDataModel.getParameters();
            List<Object> parametersJson = new ArrayList<>();
            api.put("parameters",parametersJson);
            for (Map<String,String> parameter:parameters
            ) {
                Map<String, Object> parameterjson = new HashMap<>();
                parametersJson.add(parameterjson);
                parameterjson.put("name",parameter.get("name"));
                parameterjson.put("type",parameter.get("type"));
                parameterjson.put("annotation",parameter.get("annotation"));
            }
            api.put("return_type",apiDataModel.getReturnType());
            api.put("file",apiDataModel.getFile());
            api.put("controller",apiDataModel.getController());
            api.put("description",apiDataModel.getDescription());
        }
        apiDataReport.put("api_data",apiData);
        return apiDataReport;
    }

}
