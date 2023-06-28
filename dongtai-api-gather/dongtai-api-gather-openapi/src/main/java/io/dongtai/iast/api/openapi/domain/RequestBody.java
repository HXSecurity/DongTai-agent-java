package io.dongtai.iast.api.openapi.domain;

import java.util.Map;

/**
 * @author CC11001100
 * @since v1.12.0
 */
public class RequestBody {

    private String description;
    private Map<String, MediaType> content;
    private Boolean required;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, MediaType> getContent() {
        return content;
    }

    public void setContent(Map<String, MediaType> content) {
        this.content = content;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
