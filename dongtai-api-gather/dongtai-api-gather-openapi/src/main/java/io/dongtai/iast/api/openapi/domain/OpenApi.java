package io.dongtai.iast.api.openapi.domain;

import com.alibaba.fastjson2.JSON;

import java.util.HashMap;
import java.util.Map;

/**
 * 表示一个Open Api文档
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class OpenApi {

    // 当前（2023-6-19）默认使用3.0.1版本
    public static final String DEFAULT_OPENAPI_VERSION = "3.0.1";

    private String openapi;

    private Info info;

    private Map<String, Path> paths;

    private Map<String, Map<String, Schema>> components;

    public OpenApi() {
        openapi = DEFAULT_OPENAPI_VERSION;
    }

    /**
     * 转为JSON字符串
     *
     * @return
     */
    public String toJsonString() {
        return JSON.toJSONString(this);
    }

    public void setComponentsBySchemaMap(Map<String, Schema> schemas) {
        Map<String, Map<String, Schema>> components = new HashMap<>();
        components.put("schemas", schemas);
        this.setComponents(components);
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public Map<String, Map<String, Schema>> getComponents() {
        return components;
    }

    public void setComponents(Map<String, Map<String, Schema>> components) {
        this.components = components;
    }

    public String getOpenapi() {
        return openapi;
    }

    public void setOpenapi(String openapi) {
        this.openapi = openapi;
    }

    public Map<String, Path> getPaths() {
        return paths;
    }

    public void setPaths(Map<String, Path> paths) {
        this.paths = paths;
    }

}
