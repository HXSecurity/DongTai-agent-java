package io.dongtai.iast.api.openapi.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表示一个路径方法的映射处理操作
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class Operation {

    private List<String> tags;
    private String operationId;
    private String summary;
    private String description;

    // 此映射接受的参数
    private List<Parameter> parameters;

    // 请求体
    private RequestBody requestBody;

    // 响应
    private Map<String, Response> responses;

    /**
     * 参数合并
     *
     * @param parameters
     */
    public void mergeParameters(List<Parameter> parameters) {

        // 为空的话就不合并了
        if (parameters == null || parameters.isEmpty()) {
            return;
        }

        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
            this.parameters.addAll(parameters);
            return;
        }

        // 要保持参数的顺序
        Map<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < this.parameters.size(); i++) {
            Parameter parameter = this.parameters.get(i);
            indexMap.put(parameter.getName(), i);
        }
        List<Parameter> leftParameters = new ArrayList<>();
        for (Parameter p : parameters) {
            if (indexMap.containsKey(p.getName())) {
                // 替换
                this.parameters.set(indexMap.get(p.getName()), p);
            } else {
                // 追加
                leftParameters.add(p);
            }
        }
        this.parameters.addAll(leftParameters);
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    public Map<String, Response> getResponses() {
        return responses;
    }

    public void setResponses(Map<String, Response> responses) {
        this.responses = responses;
    }
}
