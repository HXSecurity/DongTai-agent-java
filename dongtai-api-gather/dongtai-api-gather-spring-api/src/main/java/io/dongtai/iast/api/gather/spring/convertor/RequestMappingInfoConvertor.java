package io.dongtai.iast.api.gather.spring.convertor;

import io.dongtai.iast.api.openapi.convertor.OpenApiSchemaConvertorManager;
import io.dongtai.iast.api.openapi.domain.*;
import io.dongtai.log.DongTaiLog;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.*;
import java.util.function.Consumer;

/**
 * 用于把单个的映射信息解析为OpenApi的接口格式
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class RequestMappingInfoConvertor {

    private final OpenApiSchemaConvertorManager manager;
    private final WebApplicationContext webApplicationContext;
    private final RequestMappingInfo requestMappingInfo;
    private final HandlerMethod handlerMethod;

    // 从方法中解析出的映射
    private final Map<String, Path> pathMap;

    public RequestMappingInfoConvertor(OpenApiSchemaConvertorManager manager, WebApplicationContext webApplicationContext, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
        this.manager = manager;
        this.webApplicationContext = webApplicationContext;
        this.requestMappingInfo = requestMappingInfo;
        this.handlerMethod = handlerMethod;

        this.pathMap = new HashMap<>();
    }

    public Map<String, Path> parse() {
        // 从路径映射作为入口
        parsePathPatternsRequestCondition();
        return this.pathMap;
    }

    /**
     * 解析路径映射
     */
    private void parsePathPatternsRequestCondition() {
        try {
            // 5.3.0 版本引入
            PathPatternsRequestCondition c = requestMappingInfo.getPathPatternsCondition();
            if (c != null) {
                // 此方法映射到的所有路径
                c.getPatternValues().forEach(s -> {
                    Path path = new Path();
                    // 方法是第二级
                    parseRequestMethodsRequestCondition(path);
                    pathMap.put(buildFullUrl(s), path);
                });
            }
        } catch (Throwable e) {
//            DongTaiLog.debug("spring api path.getPathPatternsCondition router exception", e);
        }

        try {
            // 3.1.0.RELEASE 引入
            PatternsRequestCondition c = requestMappingInfo.getPatternsCondition();
            if (c != null) {
                c.getPatterns().forEach(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        Path path = new Path();
                        // 方法是第二级
                        parseRequestMethodsRequestCondition(path);
                        pathMap.put(buildFullUrl(s), path);
                    }
                });
            }
        } catch (Throwable e) {
//            DongTaiLog.debug("spring api path.getPatternsCondition router exception", e);
        }
    }

    private String buildFullUrl(String path) {
        String contextPath = this.webApplicationContext.getApplicationName();
        if (contextPath == null || contextPath.isEmpty()) {
            return path;
        } else {
            return contextPath + path;
        }
    }

    /**
     * 根据请求方法扩展为多个
     */
    private void parseRequestMethodsRequestCondition(Path path) {
        try {
            RequestMethodsRequestCondition c = this.requestMappingInfo.getMethodsCondition();
            Set<RequestMethod> methods = c.getMethods();
            // 如果此处默认为空的话，则将其扩展为所有的情况
            if (methods.isEmpty()) {
                // 2023-7-4 12:31:06 默认情况下认为方法不映射trace
                methods = new HashSet<>(Arrays.asList(RequestMethod.GET,
                        RequestMethod.HEAD,
                        RequestMethod.POST,
                        RequestMethod.PUT,
                        RequestMethod.PATCH,
                        RequestMethod.DELETE,
                        RequestMethod.OPTIONS
                ));
            }
            methods.forEach(new Consumer<RequestMethod>() {
                @Override
                public void accept(RequestMethod requestMethod) {
                    Operation operation = parseOperation();
                    switch (requestMethod) {
                        case GET:
                            path.setGet(operation);
                            break;
                        case HEAD:
                            path.setHead(operation);
                            break;
                        case POST:
                            path.setPost(operation);
                            break;
                        case PUT:
                            path.setPut(operation);
                            break;
                        case PATCH:
                            path.setPatch(operation);
                            break;
                        case DELETE:
                            path.setDelete(operation);
                            break;
                        case OPTIONS:
                            path.setOptions(operation);
                            break;
                        // 2023-7-4 12:30:52 忽略所有的trace方法
//                        case TRACE:
//                            path.setTrace(operation);
//                            break;
                    }
                }
            });
        } catch (Throwable e) {
//            DongTaiLog.debug("spring api method router exception", e);
        }
    }

    /**
     * 解析映射操作
     *
     * @return
     */
    private Operation parseOperation() {
        Operation operation = new Operation();

        // 请求参数路由
        try {
            List<Parameter> parameterList = parseParamsRequestCondition();
            operation.mergeParameters(parameterList);
        } catch (Throwable e) {
            DongTaiLog.debug("Spring MVC RequestMappingInfoConvertor.parseOperation parseParamsRequestCondition exception", e);
        }

        // 请求头参数
        try {
            List<Parameter> parameterList = parseHeadersRequestCondition(operation);
            operation.mergeParameters(parameterList);
        } catch (Throwable e) {
            DongTaiLog.debug("Spring MVC RequestMappingInfoConvertor.parseOperation parseHeadersRequestCondition exception", e);
        }

        // 使用随机的id
        operation.setOperationId(UUID.randomUUID().toString());

        // 全路径类名放在tags中
        operation.setTags(Collections.singletonList(this.handlerMethod.getBeanType().getName()));

        try {
            // 解析HandlerMethod
            new HandlerMethodConvertor(this.manager, this.webApplicationContext, operation, this.handlerMethod).parse();
        } catch (Throwable e) {
            DongTaiLog.debug("Spring MVC RequestMappingInfoConvertor.parseOperation HandlerMethodConvertor exception", e);
        }

        return operation;
    }

    /**
     * 解析参数路由的情况，将其作为普通的query上的参数，会加个注释，其它部分都当做普通参数处理
     */
    private List<Parameter> parseParamsRequestCondition() {
        try {
            ParamsRequestCondition paramsCondition = this.requestMappingInfo.getParamsCondition();
            List<Parameter> parameterList = new ArrayList<>();
            paramsCondition.getExpressions().forEach(new Consumer<NameValueExpression<String>>() {
                @Override
                public void accept(NameValueExpression<String> stringNameValueExpression) {
                    // 当做是一个普通的参数传递
                    Parameter p = new Parameter();
                    p.setName(stringNameValueExpression.getName());
                    p.setIn(ParameterIn.Query);
                    p.setRequired(true);
                    // 参数路由的参数统一认为是string类型
                    p.setSchema(new Schema(DataType.String()));
                    parameterList.add(p);
                }
            });
            return parameterList;
        } catch (Throwable e) {
//            DongTaiLog.debug("spring api parameters router exception: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    //	@Nullable
    //	private final PatternsRequestCondition patternsCondition;
    //
    //	private final RequestConditionHolder customConditionHolder;

    /**
     * 解析请求头路由，认为请求头路由就是一个普通的header参数
     */
    private List<Parameter> parseHeadersRequestCondition(Operation operation) {
        try {
            HeadersRequestCondition c = this.requestMappingInfo.getHeadersCondition();
            List<Parameter> headerParameterList = new ArrayList<>();
            c.getExpressions().forEach(new Consumer<NameValueExpression<String>>() {
                @Override
                public void accept(NameValueExpression<String> stringNameValueExpression) {
                    stringNameValueExpression.getName();
                    Parameter p = new Parameter();
                    p.setName(stringNameValueExpression.getName());
                    p.setRequired(true);
                    p.setIn(ParameterIn.Header);
                    // 在请求头上的参数都认为是string类型的
                    p.setSchema(new Schema(DataType.String()));
                    headerParameterList.add(p);
                }
            });
            return headerParameterList;
        } catch (Throwable e) {
//            DongTaiLog.debug("spring api headers router exception: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * consumer路由，下个版本再支持
     */
    private void parseConsumesRequestCondition(Operation operation) {
        // TODO 2023-6-19 16:20:23 暂不解析
//        ConsumesRequestCondition c = this.requestMappingInfo.getConsumesCondition();
//        if (c == null) {
//            return;
//        }
//        c.getExpressions().forEach(new Consumer<MediaTypeExpression>() {
//            @Override
//            public void accept(MediaTypeExpression mediaTypeExpression) {
//                mediaTypeExpression.getMediaType()
//            }
//        });
    }

    /**
     * producer路由，下个版本再支持
     */
    private void parseProducesRequestCondition() {
        // TODO 2023-6-19 16:20:40 暂不解析
//        ProducesRequestCondition c = this.requestMappingInfo.getProducesCondition();
//        if (c == null) {
//            return;
//        }
    }

}
