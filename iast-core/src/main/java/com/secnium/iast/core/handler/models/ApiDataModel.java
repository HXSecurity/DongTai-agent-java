package com.secnium.iast.core.handler.models;

import java.util.Map;

/**
 * 转换接口数据集为对象
 *
 * @author niuerzhuang@huoxian.cn
 */
public class ApiDataModel {

    private String url;
    private String method;
    private String clazz;
    private Map<String,String>[] parameters;
    private String returnType;
    private String file;
    private String controller;

    public ApiDataModel() {
    }


    public ApiDataModel(String url, String method, String clazz, Map<String, String>[] parameters, String returnType, String file, String controller) {
        this.url = url;
        this.method = method;
        this.clazz = clazz;
        this.parameters = parameters;
        this.returnType = returnType;
        this.file = file;
        this.controller = controller;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String>[] getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String>[] parameters) {
        this.parameters = parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }
}
