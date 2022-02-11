package cn.huoxian.iast.spring;

import io.dongtai.log.DongTaiLog;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
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
        Map<String, RequestMappingHandlerMapping> requestMappings = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, RequestMappingHandlerMapping.class, true, false);
        LocalVariableTableParameterNameDiscoverer methodParameters = new LocalVariableTableParameterNameDiscoverer();
        List<ApiDataModel> apiList = new ArrayList<>();
        RequestMappingHandlerMapping handlerMapping = requestMappings.get("requestMappingHandlerMapping");
        if (handlerMapping != null) {
            Map<RequestMappingInfo, HandlerMethod> methodMap = handlerMapping.getHandlerMethods();
            for (RequestMappingInfo info : methodMap.keySet()) {
                ApiDataModel apiDataModel = new ApiDataModel();
                HandlerMethod handlerMethod = methodMap.get(info);
                String clazz = handlerMethod.getBeanType().toString().substring(6);
                apiDataModel.setClazz(clazz);
                String method = info.getMethodsCondition().toString().replace("[", "").replace("]", "");
                String[] methods;
                if ("".equals(method)) {
                    methods = new String[]{"GET", "POST"};
                }else if (method.contains(" || ")){
                    methods = method.split(" \\|\\| ");
                } else {
                    methods = new String[]{method};
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
                    String[] params = methodParameters.getParameterNames(methodData);
                    int i = 0;
                    for (Parameter parameter : parameters
                    ) {
                        Map<String, String> parameterMap = new HashMap<>();
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
                            switch (anno) {
                                case "PathVariable":
                                    anno = "restful访问参数";
                                    break;
                                case "RequestHeader":
                                    anno = "Header参数";
                                    break;
                                case "CookieValue":
                                    anno = "Cookie参数";
                                    break;
                                case "RequestParam":
                                    anno = "GET请求参数";
                                    break;
                                case "RequestBody":
                                    anno = "POST请求的body参数";
                                    break;
                                case "Validated":
                                    anno = "GET请求参数对象";
                                    break;
                            }
                            annos.append(anno);
                        }
                        if (params != null){
                            parameterMap.put("name", params[i]);
                        }else {
                            parameterMap.put("name", "null");
                        }
                        parameterMap.put("type", classType);
                        parameterMap.put("annotation", String.valueOf(annos));
                        parameterMaps.add(parameterMap);
                        i = i + 1;
                    }
                    apiDataModel.setParameters(parameterMaps);
                    String returnType = declaredMethod.getReturnType().toString();
                    if (returnType.contains("class ")) {
                        returnType = declaredMethod.getReturnType().toString().substring(6);
                    }
                    apiDataModel.setReturnType(returnType);
                } catch (NoSuchMethodException e) {
                    DongTaiLog.error(e.getMessage());
                }


                PatternsRequestCondition patternsCondition = info.getPatternsCondition();
                Set<String> patterns = patternsCondition.getPatterns();
                if (patterns.size() > 1) {
                    for (String s : patterns
                    ) {
                        String uri = applicationContext.getApplicationName() + s.replace("[", "").replace("]", "");
                        apiDataModel.setUrl(uri);
                        apiList.add(apiDataModel);
                    }
                } else {
                    String uri = applicationContext.getApplicationName() + info.getPatternsCondition().toString().replace("[", "").replace("]", "");
                    apiDataModel.setUrl(uri);
                    apiList.add(apiDataModel);
                }
            }
        }
        return apiList;
    }

    private static Map<String, Object> createReport(List<ApiDataModel> apiList) {
        Map<String, Object> apiDataReport = new HashMap<>();
        List<Object> apiData = new ArrayList<>();
        for (ApiDataModel apiDataModel : apiList
        ) {
            Map<String, Object> api = new HashMap<>();
            apiData.add(api);
            api.put("uri", apiDataModel.getUrl());
            String[] methods = apiDataModel.getMethod();
            List<Object> methodsjson = new ArrayList<>(Arrays.asList(methods));
            api.put("method", methodsjson);
            api.put("class", apiDataModel.getClazz());
            List<Map<String, String>> parameters = apiDataModel.getParameters();
            List<Object> parametersJson = new ArrayList<>();
            api.put("parameters", parametersJson);
            for (Map<String, String> parameter : parameters
            ) {
                Map<String, Object> parameterjson = new HashMap<>();
                parametersJson.add(parameterjson);
                parameterjson.put("name", parameter.get("name"));
                parameterjson.put("type", parameter.get("type"));
                parameterjson.put("annotation", parameter.get("annotation"));
            }
            api.put("returnType", apiDataModel.getReturnType());
            api.put("file", apiDataModel.getFile());
            api.put("controller", apiDataModel.getController());
            api.put("description", apiDataModel.getDescription());
        }
        apiDataReport.put("apiData", apiData);
        return apiDataReport;
    }

}
