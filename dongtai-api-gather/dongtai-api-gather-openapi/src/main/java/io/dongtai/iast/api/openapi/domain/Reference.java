package io.dongtai.iast.api.openapi.domain;

/**
 * @author CC11001100
 * @since v1.12.0
 */
public class Reference {

    private String $ref;
    private String summary;
    private String description;

    public String get$ref() {
        return $ref;
    }

    public void set$ref(String $ref) {
        this.$ref = $ref;
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

}
