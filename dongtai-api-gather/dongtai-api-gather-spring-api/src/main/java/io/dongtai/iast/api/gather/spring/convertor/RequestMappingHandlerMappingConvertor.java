package io.dongtai.iast.api.gather.spring.convertor;

import io.dongtai.iast.api.openapi.convertor.OpenApiSchemaConvertorManager;
import io.dongtai.iast.api.openapi.domain.Info;
import io.dongtai.iast.api.openapi.domain.OpenApi;
import io.dongtai.iast.api.openapi.domain.Path;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author CC11001100
 * @since v1.12.0
 */
public class RequestMappingHandlerMappingConvertor {

    private final WebApplicationContext webApplicationContext;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    private final OpenApiSchemaConvertorManager manager;
    private final OpenApi openApi;

    public RequestMappingHandlerMappingConvertor(WebApplicationContext webApplicationContext, RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.webApplicationContext = webApplicationContext;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;

        this.manager = new OpenApiSchemaConvertorManager();
        this.openApi = new OpenApi();
    }

    /**
     * 解析整个映射
     *
     * @return
     */
    public OpenApi parse() {
        Map<String, Path> pathMap = new HashMap<>();
        requestMappingHandlerMapping.getHandlerMethods().forEach(new BiConsumer<RequestMappingInfo, HandlerMethod>() {
            @Override
            public void accept(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
                Map<String, Path> requestMappingPatMap = new RequestMappingInfoConvertor(manager, webApplicationContext, requestMappingInfo, handlerMethod).parse();
                // 合并路径映射，需要考虑到路径映射重复的情况将其合并
                merge(pathMap, requestMappingPatMap);
            }
        });
        this.openApi.setPaths(pathMap);

        // 设置组件数据库
        this.openApi.setComponentsBySchemaMap(this.manager.getDatabase().toComponentSchemasMap());

        // info信息是必须携带的
        Info info = new Info();
        info.setTitle("OpenAPI definition");
        this.openApi.setInfo(info);

        return this.openApi;
    }

    /**
     * 合并解析出的两个映射，比如可能会出现同一个路径上不同的HTTP方法映射到不同的HandlerMethod的情况，这种情况下就需要合并为一个
     */
    private void merge(Map<String, Path> m1, Map<String, Path> m2) {
        m2.forEach(new BiConsumer<String, Path>() {
            @Override
            public void accept(String s, Path path) {
                Path existsPath = m1.get(s);
                if (existsPath == null) {
                    m1.put(s, path);
                    return;
                }
                existsPath.merge(path);
            }
        });
    }

}
