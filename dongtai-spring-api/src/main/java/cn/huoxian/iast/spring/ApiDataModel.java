package cn.huoxian.iast.spring;

import java.util.List;
import java.util.Map;

/**
 * 转换接口数据集为对象
 *
 * @author niuerzhuang@huoxian.cn
 */
public class ApiDataModel {

    private String url;
    private String[] method;
    private String clazz;
    List<Map<String, String>> parameters;
    private String returnType;
    private String file;
    private String controller;
    private String description;

    public ApiDataModel() {
    }

    public ApiDataModel(String url, String[] method, String clazz, List<Map<String, String>> parameters, String returnType, String file, String controller, String description) {
        this.url = url;
        this.method = method;
        this.clazz = clazz;
        this.parameters = parameters;
        this.returnType = returnType;
        this.file = file;
        this.controller = controller;
        this.description = description;
    }

    public String getDescription() {
        if (description == null) {
            description = "";
        }
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClazz() {
        if (clazz == null) {
            clazz = "";
        }
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

    public String[] getMethod() {
        return method;
    }

    public void setMethod(String[] method) {
        this.method = method;
    }

    public List<Map<String, String>> getParameters() {
        return parameters;
    }

    public void setParameters(List<Map<String, String>> parameters) {
        this.parameters = parameters;
    }

    public String getReturnType() {
        if (returnType == null || returnType.equals("")) {
            returnType = "";
        }
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getFile() {
        if (file == null) {
            file = "";
        }
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getController() {
        if (controller == null) {
            controller = "";
        }
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }
}
